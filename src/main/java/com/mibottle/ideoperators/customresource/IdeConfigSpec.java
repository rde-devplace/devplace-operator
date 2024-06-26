/*
 * Copyright (c) 2023 himang10@gmail.com, Yi Yongwoo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mibottle.ideoperators.customresource;

import lombok.Data;

import java.util.List;

@Data
public class IdeConfigSpec {

    private String userName;
    // v2beta1 에 추가
    private String wsName;  // workspace Name
    private String appName; // application Name

    private List<String> serviceTypes;
    private WebSSH webssh;
    private Vscode vscode;
    private List<Port> portList;
    private InfrastructureSize infrastructureSize;
    private int replicas;
}

