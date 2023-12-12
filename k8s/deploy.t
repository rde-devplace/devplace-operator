apiVersion: apps/v1
kind: Deployment
metadata:
  name: ${IMAGE_NAME}
spec:
  replicas: 1
  selector:
    matchLabels:
      app: ${IMAGE_NAME}
  template:
    metadata:
      annotations:
        prometheus.io/scrape: 'true'
        prometheus.io/port: '8081'
        prometheus.io/path: '/actuator/prometheus'
        update: ${HASHCODE}
      labels:
        app: ${IMAGE_NAME}
    spec:
      serviceAccountName: cluster-admin-sa
      imagePullSecrets:
      - name: harbor-registry-secret
      containers:
      - name: ${IMAGE_NAME}
        image: ${DOCKER_REGISTRY}/${IMAGE_NAME}:${VERSION}
        imagePullPolicy: Always
        securityContext:
            runAsUser: 0
            privileged: true
        env:
        - name: COMDEV_PVC_NAME
          value: "com-dev-pvc"
        - name: USER_STORAGE_CLASS_NAME
          value: "gp2"
        - name: VSCODE_IMAGE_PATH
          value: "${DOCKER_REGISTRY}/amdp-vscode-server:1.0"
        - name: SSHSERVER_IMAGE_PATH
          value: "${DOCKER_REGISTRY}/ssh-with-k9s-extend:1.0"
        - name: WETTY_IMAGE_PATH
          value: "${DOCKER_REGISTRY}/wetty-with-k9s:1.0"