package org.bonitasoft.web.rest.server.api.system;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;

/**
 * @author Vincent Elcrin
 */
@RunWith(MockitoJUnitRunner.class)
public class UserRightsBuilderTest {

    SHA1Generator generator = new SHA1Generator();

    @Mock
    org.bonitasoft.engine.session.APISession session;

    @InjectMocks
    UserRightsBuilder builder;

    @Test
    public void should_return_a_list_of_sba1ed_rights_composed_from_tokens_concatenated_with_session_id() throws Exception {
        given(session.getId()).willReturn(3L);

        List<String> rights = builder.buildFrom(Arrays.asList("token"));

        assertEquals(rights.get(0), generator.getHash("token3"));
    }
}
