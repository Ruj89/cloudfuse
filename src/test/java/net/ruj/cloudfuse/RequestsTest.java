package net.ruj.cloudfuse;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;

public class RequestsTest {

    @Test
    public void uriEncoding() throws Exception {
        LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        ArrayList<String> value = new ArrayList<>();
        value.add("hello+world");
        params.put("q", value);
        URI s = UriComponentsBuilder.fromUriString("http://example.com/try")
                .queryParams(params).build().toUri();
        Assertions.assertThat(s.toString()).isEqualTo("http://example.com/prova?q=ciao+mondo");
    }

}
