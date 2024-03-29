#!/bin/bash

# 입력 파라미터 확인
if [ -z "$1" ] || { [ "$1" != "create" ] && [ "$1" != "delete" ]; }; then
    exit 1
fi

ACTION=$1
# Environment variables and settings
KEYCLOAK_URL="https://console-dev.amdp-dev.cloudzcp.io/iam"
CLIENT_ID="kube-proxy-renew"
CLIENT_SECRET="B3x4C4DARIYPVemmXghmxZZdFGhfofWN"
USERNAME="himang10"
PASSWORD="ywyi1004"

OPERATOR_URL=kube-proxy.amdp-dev.skamdp.org
USERID="208b944e-0c8d-44f7-a495-9cb12c530ee5"
SA="cluster-admin-sa"

# Name 변수를 입력받습니다. 기본값: himang10
read -p "Enter the name (default: himang10): " USERNAME
USERNAME=${USERNAME:-himang10}
read -p "Enter the password (default: 1234): " PASSWORD
PASSWORD=${PASSWORD:-1234}

# Namespace 변수를 입력받습니다. 기본값: kube-pattern
read -p "Enter the namespace (default: kube-pattern): " NAMESPACE
NAMESPACE=${NAMESPACE:-kube-pattern}

# JSON body를 구성합니다.
json_payload=$(cat <<EOF
{
  "userName": "$USERNAME",
  "userId": "$USERID",
  "serviceAccountName": "$SA",
  "portList": [
    {
      "name": "examplePortName",
      "protocol": "exampleProtocol",
      "port": 8080,
      "targetPort": 8000
    }
  ],
  "infrastructureSize": {
    "cpu": "200m",
    "memory": "512Mi",
    "disk": "20Gi"
  },
  "replicas": 1
}
EOF
)


# Keycloak에서 액세스 토큰을 얻습니다.
TOKEN_RESPONSE=$(curl -s -X POST "$KEYCLOAK_URL/realms/amdp-dev/protocol/openid-connect/token" \
     -H "Content-Type: application/x-www-form-urlencoded" \
     -d "client_id=$CLIENT_ID" \
     -d "client_secret=$CLIENT_SECRET" \
     -d "username=$USERNAME" \
     -d "password=$PASSWORD" \
     -d "grant_type=client_credentials")

echo -e "Token Response:\n$(echo "$TOKEN_RESPONSE" | jq)\n"

ACCESS_TOKEN=$(echo $TOKEN_RESPONSE | jq -r .access_token)
[ -z "$ACCESS_TOKEN" ] && echo "Failed to retrieve access token." && exit 1

echo $ACCESS_TOKEN


# 요청 유형에 따른 처리
if [ "$ACTION" == "create" ]; then
    curl -X POST "$OPERATOR_URL/api/ideconfig/ide?name=$USERNAME&namespace=$NAMESPACE" \
         -H "Content-Type: application/json" \
         -H "Authorization: Bearer $ACCESS_TOKEN" \
         -d "$json_payload"
elif [ "$ACTION" == "delete" ]; then
    curl -X DELETE "$OPERATOR_URL/api/ideconfig/ide?name=$USERNAME&namespace=$NAMESPACE" \
        -H "Authorization: Bearer $ACCESS_TOKEN"
fi

echo ""  # 결과 출력 후 줄바꿈을 위해

