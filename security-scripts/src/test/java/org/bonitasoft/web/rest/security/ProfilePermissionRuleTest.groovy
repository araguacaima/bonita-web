/**
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 **/


package org.bonitasoft.web.rest.security

import org.bonitasoft.engine.api.APIAccessor
import org.bonitasoft.engine.api.Logger
import org.bonitasoft.engine.api.ProfileAPI
import org.bonitasoft.engine.api.permission.APICallContext
import org.bonitasoft.engine.identity.User
import org.bonitasoft.engine.profile.ProfileCriterion
import org.bonitasoft.engine.profile.impl.ProfileImpl
import org.bonitasoft.engine.session.APISession
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.runners.MockitoJUnitRunner

import static org.assertj.core.api.Assertions.assertThat
import static org.mockito.Mockito.doReturn
import static org.mockito.Mockito.when

@RunWith(MockitoJUnitRunner.class)
public class ProfilePermissionRuleTest {

    @Mock
    def APISession apiSession
    @Mock
    def APICallContext apiCallContext
    @Mock
    def APIAccessor apiAccessor
    @Mock
    def Logger logger
    def ProfilePermissionRule rule = new ProfilePermissionRule()
    @Mock
    def ProfileAPI profileAPI
    @Mock
    def User user
    def long currentUserId = 16l

    @Before
    public void before() {
        doReturn(profileAPI).when(apiAccessor).getProfileAPI()
        doReturn(currentUserId).when(apiSession).getUserId()
    }

    @Test
    public void should_check_verify_get_is_true_when_user_id_in_filters() {
        doReturn(true).when(apiCallContext).isGET()
        doReturn([user_id: "16"]).when(apiCallContext).getFilters()

        //when
        def isAuthorized = rule.check(apiSession, apiCallContext, apiAccessor, logger)
        //then
        assertThat(isAuthorized).isTrue();
    }

    @Test
    public void should_check_verify_get_with_resource_user_is_in_profile() {
        doReturn(true).when(apiCallContext).isGET()
        doReturn("2").when(apiCallContext).getResourceId()
        doReturn([user_id: "16"]).when(apiCallContext).getFilters()
        doReturn([profile(1), profile(2), profile(3)]).when(profileAPI).getProfilesForUser(currentUserId, 0, 100, ProfileCriterion.ID_ASC)
        //when
        def isAuthorized = rule.check(apiSession, apiCallContext, apiAccessor, logger)
        //then
        assertThat(isAuthorized).isTrue();
    }

    @Test
    public void should_check_verify_get_with_resource_user_is_in_profile_with_more_than_100_elements() {
        doReturn(true).when(apiCallContext).isGET()
        doReturn("110").when(apiCallContext).getResourceId()

        doReturn((1l..100l).collect {
            profile(it)
        }).when(profileAPI).getProfilesForUser(currentUserId, 0, 100, ProfileCriterion.ID_ASC)
        doReturn((101l..110l).collect {
            profile(it)
        }).when(profileAPI).getProfilesForUser(currentUserId, 100, 100, ProfileCriterion.ID_ASC)
        //when
        def isAuthorized = rule.check(apiSession, apiCallContext, apiAccessor, logger)
        //then
        assertThat(isAuthorized).isTrue();
    }

    @Test
    public void should_check_verify_get_with_resource_user_is_not_in_profile() {
        doReturn(true).when(apiCallContext).isGET()
        doReturn("2").when(apiCallContext).getResourceId()
        doReturn([profile(1), profile(3)]).when(profileAPI).getProfilesForUser(currentUserId, 0, 100, ProfileCriterion.ID_ASC)
        //when
        def isAuthorized = rule.check(apiSession, apiCallContext, apiAccessor, logger)
        //then
        assertThat(isAuthorized).isFalse();
    }

    @Test
    public void should_check_verify_not_get_return_false() {
        doReturn(false).when(apiCallContext).isGET()
        doReturn(true).when(apiCallContext).isPOST()
        doReturn(true).when(apiCallContext).isDELETE()
        doReturn(true).when(apiCallContext).isPUT()
        //when
        def isAuthorized = rule.check(apiSession, apiCallContext, apiAccessor, logger)
        //then
        assertThat(isAuthorized).isFalse();
    }

    private ProfileImpl profile(long id) {
        def profile = new ProfileImpl("profilename")
        profile.setId(id)
        return profile
    }


}
