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
import org.bonitasoft.engine.api.permission.APICallContext
import org.bonitasoft.engine.api.permission.PermissionRule
import org.bonitasoft.engine.session.APISession

/**
 *
 * Let a user view and add process su only if he is process owner
 *
 * <ul>
 *     <li>bpm/processSupervisor</li>
 * </ul>
 *
 *
 *
 * @author Anthony Birembaut
 */
class ProcessSupervisorPermissionRule implements PermissionRule {

    public static final String PROCESS_ID = "process_id"

    @Override
    public boolean check(APISession apiSession, APICallContext apiCallContext, APIAccessor apiAccessor, Logger logger) {
        long currentUserId = apiSession.getUserId();
        if (apiCallContext.isPOST()) {
            return checkPostMethod(apiCallContext, apiAccessor, currentUserId, logger)
        } else if (apiCallContext.isGET()) {
            return checkGetMethod(apiCallContext, apiAccessor, currentUserId, logger)
        } else if(apiCallContext.isDELETE()) {
            return checkDeleteMethod(apiCallContext, apiAccessor, currentUserId, logger)
        }
        //it's ok to read
        return true
    }

    private boolean checkPostMethod(APICallContext apiCallContext, APIAccessor apiAccessor, long currentUserId, Logger logger) {
        def body = apiCallContext.getBodyAsJSONArray()
        def processAPI = apiAccessor.getProcessAPI()
        for (int i = 0 ; i < body.length(); i++){
            def object = body.getJSONObject(i)
            def processId = Long.valueOf(object.optString(PROCESS_ID))
            if (processId <= 0) {
                continue
            }
            if(!processAPI.isUserProcessSupervisor(processId,currentUserId)){
                return false
            }
        }
        return true
    }

    private boolean checkGetMethod(APICallContext apiCallContext, APIAccessor apiAccessor, long currentUserId, Logger logger) {
        def filters = apiCallContext.getFilters()
        if (filters.containsKey(PROCESS_ID)) {
            def processAPI = apiAccessor.getProcessAPI()
            return processAPI.isUserProcessSupervisor(Long.parseLong(filters.get(PROCESS_ID)),currentUserId)
        }
        return true
    }

    private boolean checkDeleteMethod(APICallContext apiCallContext, APIAccessor apiAccessor, long currentUserId, Logger logger) {
        def resourceIds = apiCallContext.getCompoundResourceId()
        if (!resourceIds.isEmpty()) {
            def processAPI = apiAccessor.getProcessAPI()
            return processAPI.isUserProcessSupervisor(Long.parseLong(resourceIds.get(0)),currentUserId)
        }
        return true
    }
}
