def createNamespace(Map configNameSpace) {
    sh "kubectl get namespace --kubeconfig ~/.kube/config_${configNameSpace.CLUSTER_NAME} | grep -q '^${configNameSpace.NAMESPACE} ' || kubectl create namespace --kubeconfig ~/.kube/config_${configNameSpace.CLUSTER_NAME} ${configNameSpace.NAMESPACE}"
}

def deployRelease(Map configRelease) {
    sh "cd ./kubernetes/helm/k8s && helm upgrade --kubeconfig ~/.kube/config_${configRelease.CLUSTER_NAME} --install -f ./values.yaml ${configRelease.RELEASE_NAME} --set=image.repository=${configRelease.REPOSITORY_URI} --set=image.tag=${configRelease.IMAGE_TAG} --namespace ${configRelease.NAMESPACE} . "
    sh '''#!/bin/bash
    while [[ -z "$(kubectl get svc --kubeconfig ~/.kube/config_''' + configRelease.CLUSTER_NAME + ''' -n ''' + configRelease.NAMESPACE + ''' -o jsonpath='{.items[0].status.loadBalancer.ingress[0].hostname}' -l="app.kubernetes.io/instance=''' + configRelease.RELEASE_NAME + '''")" ]]; do
    echo "Waiting for external IP"
    sleep 3
    done
    echo "External IP ready"
    kubectl get svc --kubeconfig ~/.kube/config_''' + configRelease.CLUSTER_NAME + ''' -n ''' + configRelease.NAMESPACE + ''' -l="app.kubernetes.io/instance=''' + configRelease.RELEASE_NAME + '''"
    '''
}

def removeRelease(Map configRemoveRelease) {
    sh "helm uninstall --kubeconfig ~/.kube/config_${configRemoveRelease.CLUSTER_NAME} ${configRemoveRelease.RELEASE_NAME} --namespace ${configRemoveRelease.NAMESPACE}"
}

def pushImageToECR(Map config) {
    sh """aws ecr describe-repositories --repository-names ${config.ECR_REPO_NAME} || aws ecr create-repository --repository-name ${config.ECR_REPO_NAME}"""
    sh """aws ecr get-login-password --region ${config.AWS_REGION} | docker login --username AWS --password-stdin ${config.AWS_ACCOUNT_ID}.dkr.ecr.${config.AWS_REGION}.amazonaws.com"""
    sh """docker push ${config.AWS_ACCOUNT_ID}.dkr.ecr.${config.AWS_REGION}.amazonaws.com/${config.ECR_REPO_NAME}:${IMAGE_TAG} """
}

def updateKubeconfig(Map configKube) {
    sh """aws eks update-kubeconfig --kubeconfig ~/.kube/config_${configKube.CLUSTER_NAME} --name ${configKube.CLUSTER_NAME} --region ${configKube.AWS_REGION} """
}

def listResource(Map configlistResource ) {
    sh "kubectl get --kubeconfig ~/.kube/config_${configlistResource.CLUSTER_NAME} ${configlistResource.RESOURCE} -n ${configlistResource.NAMESPACE} -l='${configlistResource.LABELS}' "
}
