package com.sonar.trading.bitso.client;

import com.sonar.trading.utils.Resources;
import org.apache.http.HttpStatus;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.InputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;

public class BitsoClientTest {
    @Mock
    private CloseableHttpClient httpClient;
    private BitsoClientConfig config = new BitsoClientConfig("dummyEndpoint");
    private BitsoClient bitsoClient;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        bitsoClient = new BitsoClient(config, httpClient);
    }

    @Test
    public void whenCallingGetTrades_andSuccessResponseIsReturned_thenResponseIsParsedCorrectly() throws Exception {
        doReturn(createHttpResponse("bitso/responses/trades_success.json")).when(httpClient).execute(any(HttpGet.class));
        BitsoResponse<List<Trade>> response = bitsoClient.getTrades("btc_mxn");
        assertThat(response.isSuccess()).isTrue();
        List<Trade> trades = response.getPayload();
        assertThat(trades).hasSize(2);
        assertThatTradeIs(trades.get(0), "btc_mxn", "2017-12-18T00:30:59+00:00", "0.02000000", "buy", "5545.01", 55845);
        assertThatTradeIs(trades.get(1), "btc_mxn", "2017-12-18T00:31:59+00:00", "0.33723939", "sell", "5633.98", 55844);
    }

    private void assertThatTradeIs(Trade trade, String book, String createdAt, String amount, String markerSide, String price, long tid) {
        assertThat(trade.getBook()).isEqualTo(book);
        assertThat(trade.getCreatedAt()).isEqualTo(createdAt);
        assertThat(trade.getAmount()).isEqualTo(amount);
        assertThat(trade.getMarkerSide()).isEqualTo(markerSide);
        assertThat(trade.getPrice()).isEqualTo(price);
        assertThat(trade.getTid()).isEqualTo(tid);
    }

    private CloseableHttpResponse createHttpResponse(String responsePath) {
        InputStream responseBodyStream = Resources.getResourceStream(responsePath);
        BasicHttpResponse httpResponse = new BasicHttpResponse(new BasicStatusLine(new ProtocolVersion("http", 2, 0), HttpStatus.SC_OK, "Ok"));
        httpResponse.setEntity(new InputStreamEntity(responseBodyStream, ContentType.APPLICATION_JSON));
        return new HttpResponseProxy(httpResponse);
    }
}
