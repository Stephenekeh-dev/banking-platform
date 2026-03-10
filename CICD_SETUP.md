CI/CD Pipeline — Setup Guide
Overview
BranchTriggerWhat happensdevPushTest all services → Build & push Docker images to ECR (tagged :dev-latest + SHA)mainPushTest → Build → Push (tagged :latest + SHA) → Deploy to EKS → Rollback on failureAny PROpen/UpdateRun all tests only (no build, no deploy)

1. GitHub Secrets Required
   Go to your repo → Settings → Secrets and variables → Actions and add:
   SecretDescriptionAWS_ACCOUNT_IDYour 12-digit AWS account IDAWS_ACCESS_KEY_IDIAM user access key with ECR + EKS permissionsAWS_SECRET_ACCESS_KEYIAM user secret key

2. Required AWS IAM Permissions
   The IAM user/role used by GitHub Actions needs these policies:

AmazonEC2ContainerRegistryFullAccess — push images to ECR
AmazonEKSClusterPolicy — update kubeconfig & deploy to EKS
Or a custom policy scoped to your specific resources


3. Create ECR Repositories (one-time setup)
   bashaws ecr create-repository --repository-name banking-platform/corebanking --region us-east-1
   aws ecr create-repository --repository-name banking-platform/audit-service --region us-east-1
   aws ecr create-repository --repository-name banking-platform/api-gate      --region us-east-1

4. Create EKS Cluster (one-time setup)
   basheksctl create cluster \
   --name banking-platform-cluster \
   --region us-east-1 \
   --nodegroup-name standard-workers \
   --node-type t3.medium \
   --nodes 2 \
   --nodes-min 1 \
   --nodes-max 4 \
   --managed

5. First-Time Kubernetes Setup
   bash# Point kubectl at your cluster
   aws eks update-kubeconfig --region us-east-1 --name banking-platform-cluster

# Create namespace
kubectl create namespace banking-platform

# Create secrets (replace placeholder values)
kubectl create secret generic banking-platform-secrets \
--namespace=banking-platform \
--from-literal=DB_URL=jdbc:postgresql://<host>:5432/bankingdb \
--from-literal=DB_USERNAME=<username> \
--from-literal=DB_PASSWORD=<password> \
--from-literal=JWT_SECRET=<your-256-bit-secret>

# Apply ConfigMap
kubectl apply -f k8s/infrastructure/configmap.yml

6. Install AWS Load Balancer Controller (for Ingress)
   Required for the ALB Ingress in k8s/api-gate/deployment.yml to work:
   bashhelm repo add eks https://aws.github.io/eks-charts
   helm repo update
   helm install aws-load-balancer-controller eks/aws-load-balancer-controller \
   -n kube-system \
   --set clusterName=banking-platform-cluster

7. Project Structure
   banking-platform/
   ├── .github/
   │   └── workflows/
   │       ├── ci.yml        # dev branch: test + build + push to ECR
   │       └── cd.yml        # main branch: test + build + push + deploy to EKS
   ├── k8s/
   │   ├── infrastructure/
   │   │   ├── configmap.yml
   │   │   └── secrets-template.yml
   │   ├── corebanking/
   │   │   └── deployment.yml
   │   ├── audit-service/
   │   │   └── deployment.yml
   │   └── api-gate/
   │       └── deployment.yml  (includes Ingress)
   ├── corebanking/
   │   └── Dockerfile
   ├── audit-service/
   │   └── Dockerfile
   └── api-gate/
   └── Dockerfile

8. How Rollback Works
   If any kubectl rollout status check fails during deployment, the pipeline automatically runs:
   bashkubectl rollout undo deployment/corebanking -n banking-platform
   kubectl rollout undo deployment/audit-service -n banking-platform
   kubectl rollout undo deployment/api-gate -n banking-platform
   This reverts each service to its previous working image instantly.