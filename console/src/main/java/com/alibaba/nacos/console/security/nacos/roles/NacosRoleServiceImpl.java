/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.nacos.console.security.nacos.roles;


import com.alibaba.nacos.config.server.auth.PermissionInfo;
import com.alibaba.nacos.config.server.auth.PermissionPersistService;
import com.alibaba.nacos.config.server.auth.RoleInfo;
import com.alibaba.nacos.config.server.auth.RolePersistService;
import com.alibaba.nacos.config.server.model.Page;
import com.alibaba.nacos.console.security.nacos.NacosAuthConfig;
import com.alibaba.nacos.console.security.nacos.users.NacosUserDetailsServiceImpl;
import com.alibaba.nacos.core.auth.Permission;
import com.alibaba.nacos.core.utils.Loggers;
import io.jsonwebtoken.lang.Collections;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Nacos builtin role service.
 *
 * @author nkorange
 * @since 1.2.0
 */
@Service
public class NacosRoleServiceImpl {

    private static final String GLOBAL_ADMIN_ROLE = "GLOBAL_ADMIN";

    @Autowired
    private RolePersistService rolePersistService;

    @Autowired
    private NacosUserDetailsServiceImpl userDetailsService;

    @Autowired
    private PermissionPersistService permissionPersistService;

    private Map<String, List<RoleInfo>> roleInfoMap = new ConcurrentHashMap<>();

    private Map<String, List<PermissionInfo>> permissionInfoMap = new ConcurrentHashMap<>();

    @Scheduled(initialDelay = 5000, fixedDelay = 15000)
    private void reload() {
        try {
            Page<RoleInfo> roleInfoPage = rolePersistService.getRolesByUserName(StringUtils.EMPTY, 1, Integer.MAX_VALUE);
            if (roleInfoPage == null) {
                return;
            }
            Set<String> roleSet = new HashSet<>(16);
            Map<String, List<RoleInfo>> tmpRoleInfoMap = new ConcurrentHashMap<>(16);
            for (RoleInfo roleInfo : roleInfoPage.getPageItems()) {
                if (!tmpRoleInfoMap.containsKey(roleInfo.getUsername())) {
                    tmpRoleInfoMap.put(roleInfo.getUsername(), new ArrayList<>());
                }
                tmpRoleInfoMap.get(roleInfo.getUsername()).add(roleInfo);
                roleSet.add(roleInfo.getRole());
            }

            Map<String, List<PermissionInfo>> tmpPermissionInfoMap = new ConcurrentHashMap<>(16);
            for (String role : roleSet) {
                Page<PermissionInfo> permissionInfoPage = permissionPersistService.getPermissions(role, 1, Integer.MAX_VALUE);
                tmpPermissionInfoMap.put(role, permissionInfoPage.getPageItems());
            }

            roleInfoMap = tmpRoleInfoMap;
            permissionInfoMap = tmpPermissionInfoMap;
        } catch (Exception e) {
            Loggers.AUTH.warn("[LOAD-ROLES] load failed", e);
        }
    }

    /**
     * Determine if the user has permission of the resource.
     * <p>
     * Note if the user has many roles, this method returns true if any one role of the user has the
     * desired permission.
     *
     * @param username user info
     * @param permission permission to auth
     * @return true if granted, false otherwise
     */
    public boolean hasPermission(String username, Permission permission) {

        List<RoleInfo> roleInfoList = roleInfoMap.get(username);
        if (Collections.isEmpty(roleInfoList)) {
            return false;
        }

        // Global admin pass:
        for (RoleInfo roleInfo : roleInfoList) {
            if (GLOBAL_ADMIN_ROLE.equals(roleInfo.getRole())) {
                return true;
            }
        }

        // Old global admin can pass resource 'console/':
        if (permission.getResource().startsWith(NacosAuthConfig.CONSOLE_RESOURCE_NAME_PREFIX)) {
            return false;
        }

        // For other roles, use a pattern match to decide if pass or not.
        for (RoleInfo roleInfo : roleInfoList) {
            List<PermissionInfo> permissionInfoList = permissionInfoMap.get(roleInfo.getRole());
            if (Collections.isEmpty(permissionInfoList)) {
                continue;
            }
            for (PermissionInfo permissionInfo : permissionInfoList) {
                String permissionResource = permissionInfo.getResource().replaceAll("\\*", ".*");
                String permissionAction = permissionInfo.getAction();
                if (permissionAction.contains(permission.getAction()) &&
                    Pattern.matches(permissionResource, permission.getResource())) {
                    return true;
                }
            }
        }
        return false;
    }

    public Page<RoleInfo> getRolesFromDatabase(String userName, int pageNo, int pageSize) {
        Page<RoleInfo> roles = rolePersistService.getRolesByUserName(userName, pageNo, pageSize);
        return roles;
    }

    public Page<PermissionInfo> getPermissionsByRoleFromDatabase(String role, int pageNo, int pageSize) {
        return permissionPersistService.getPermissions(role, pageNo, pageSize);
    }

    public void addRole(String role, String username) {
        if (userDetailsService.getUser(username) == null) {
            throw new IllegalArgumentException("user '" + username + "' not found!");
        }
        rolePersistService.addRole(role, username);
    }

    public void deleteRole(String role, String userName) {
        rolePersistService.deleteRole(role, userName);
    }

    public void deleteRole(String role) {
        rolePersistService.deleteRole(role);
    }

    public Page<PermissionInfo> getPermissionsFromDatabase(String role, int pageNo, int pageSize) {
        Page<PermissionInfo> pageInfo = permissionPersistService.getPermissions(role, pageNo, pageSize);
        return pageInfo;
    }

    public void addPermission(String role, String resource, String action) {
        permissionPersistService.addPermission(role, resource, action);
    }

    public void deletePermission(String role, String resource, String action) {
        permissionPersistService.deletePermission(role, resource, action);
    }
}
