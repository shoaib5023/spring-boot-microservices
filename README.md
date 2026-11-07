# Spring Boot Microservices

A local practice project using Spring Boot microservices, React frontends, Docker, Docker Compose, and Kubernetes.

## Contents

- [Prerequisites](#prerequisites)
- [Repository structure](#repository-structure)
- [1. Run with Docker Compose](#1-run-with-docker-compose)
- [2. Verify Docker containers](#2-verify-docker-containers)
- [3. Deploy to Kubernetes](#3-deploy-to-kubernetes)
- [4. Verify Kubernetes resources](#4-verify-kubernetes-resources)
- [5. Access the application](#5-access-the-application)
- [6. Troubleshooting](#6-troubleshooting)
- [7. Deploy with Harness](#7-deploy-with-harness)

## Prerequisites

Install and verify:

- Docker Desktop (Linux containers enabled)
- Git
- `kubectl`
- A running Kubernetes cluster, such as Docker Desktop Kubernetes
- Java and Maven only if building/running services outside Docker
- Node.js and npm only if running frontends outside Docker

Check versions:

```powershell
docker --version
docker compose version
kubectl version --client
```

From the repository root in PowerShell:

```powershell
cd H:\spring-boot-microservices
Get-ChildItem
```

## Repository structure

Your repository may look similar to this. Adjust folder names to match your actual project:

```text
spring-boot-microservices/
├── auth-service/
├── product-service/
├── auth-frontend/
├── product-frontend/
├── docker-compose.yml
└── k8s/
    ├── namespace.yaml
    ├── configmap.yaml
    ├── auth-deployment.yaml
    ├── auth-service.yaml
    ├── product-deployment.yaml
    ├── product-service.yaml
    ├── gateway-deployment.yaml
    ├── gateway-service.yaml
    └── frontend-deployment.yaml
```

Keep secrets such as database passwords, JWT signing keys, and registry credentials out of Git. Use local environment files or Kubernetes Secrets. Do not commit real credentials.

## 1. Run with Docker Compose

Run these commands from the folder containing `docker-compose.yml`.

### 1.1 Validate the Compose configuration

```powershell
docker compose config
```

If the configuration is valid, Compose prints the resolved configuration. Check any errors before continuing.

### 1.2 Build the images

```powershell
docker compose build
```

To rebuild only the authentication frontend:

```powershell
docker compose build auth-frontend
```

If the build fails at `RUN npm ci`, inspect the npm error above the final error summary. `npm ci` requires a compatible `package-lock.json`.

### 1.3 Start the containers

```powershell
docker compose up -d
```

Check status:

```powershell
docker compose ps
```

View logs:

```powershell
docker compose logs --tail=100
```

View one service's logs:

```powershell
docker compose logs --tail=100 auth-service
docker compose logs --tail=100 product-service
docker compose logs --tail=100 auth-frontend
```

Follow logs live:

```powershell
docker compose logs -f
```

Press `Ctrl+C` to stop following logs; this does not normally stop the containers.

### 1.4 Stop the containers

```powershell
docker compose down
```

To also delete Compose-managed volumes (this deletes persisted local database data):

```powershell
docker compose down --volumes
```

## 2. Verify Docker containers

```powershell
docker ps
docker compose ps
```

Check the frontend in a browser using the host port configured in `docker-compose.yml`. For example, if the Compose file maps port `5173` to the host, open:

- Auth frontend: `http://localhost:5173`

Use the actual host ports from your Compose configuration for the API and other frontend. A container being `Up` does not necessarily mean its application is healthy; inspect logs and test the relevant endpoint.

## 3. Deploy to Kubernetes

> Docker Compose and Kubernetes are separate deployment methods. Starting Compose containers does not deploy them to Kubernetes.

### 3.1 Start and check the cluster

If using Docker Desktop Kubernetes, enable Kubernetes in Docker Desktop settings and wait for the cluster to become ready.

```powershell
kubectl config get-contexts
kubectl config current-context
kubectl cluster-info
kubectl get nodes
```

If `kubectl` cannot connect, check that your cluster is running and that the current context points to the intended cluster. Do not continue until `kubectl get nodes` works.

### 3.2 Build images for Kubernetes

Kubernetes must be able to pull every image referenced in the Deployment manifests. The exact commands depend on the `image:` values in `k8s/*.yaml`.

For local Docker Desktop Kubernetes, build each image using the exact names and tags in your manifests. Example only:

```powershell
docker build -t auth-service:latest .\auth-service
docker build -t product-service:latest .\product-service
docker build -t auth-frontend:latest .\auth-frontend
```

If you have a gateway image, build it too from its actual directory.

For local images, confirm Docker Desktop Kubernetes can access them. If your cluster is remote or uses separate nodes, push images to a registry accessible to the cluster and update the manifests to use those registry image names. Configure `imagePullSecrets` if the registry is private.

### 3.3 Check Kubernetes manifest files

Confirm the manifest folder exists:

```powershell
Get-ChildItem .\k8s
```

Review the image names and configuration:

```powershell
Select-String -Path .\k8s\*.yaml -Pattern 'image:|namespace:|containerPort:|targetPort:'
```

Ensure the database hostnames, service ports, environment variables, and JWT secret configuration match your application. Pods inside Kubernetes should normally connect to databases using the relevant Kubernetes Service DNS name, not `localhost`.

### 3.4 Apply the manifests

If all manifests are in `k8s` and reference the `practice` namespace:

```powershell
kubectl apply -f .\k8s\
```

Alternatively, apply them in a deliberate order if your files are not all ready:

```powershell
kubectl apply -f .\k8s\namespace.yaml
kubectl apply -f .\k8s\configmap.yaml
kubectl apply -f .\k8s\auth-deployment.yaml
kubectl apply -f .\k8s\auth-service.yaml
kubectl apply -f .\k8s\product-deployment.yaml
kubectl apply -f .\k8s\product-service.yaml
```

Apply gateway and frontend manifests only if their images and configuration are ready:

```powershell
kubectl apply -f .\k8s\gateway-deployment.yaml
kubectl apply -f .\k8s\gateway-service.yaml
kubectl apply -f .\k8s\frontend-deployment.yaml
```

The files must exist with these exact names. If your namespace is defined in `namespace.yaml`, ensure it is applied before the namespaced resources.

## 4. Verify Kubernetes resources

Check the namespace and resources:

```powershell
kubectl get namespaces
kubectl get all -n practice
kubectl get pods -n practice -o wide
kubectl get services -n practice
kubectl get deployments -n practice
```

Wait for deployments to become ready:

```powershell
kubectl rollout status deployment/auth-service -n practice --timeout=180s
kubectl rollout status deployment/product-service -n practice --timeout=180s
```

Run rollout checks for the gateway and frontend too if those Deployments exist.

Inspect a pod that is not ready:

```powershell
kubectl describe pod <pod-name> -n practice
kubectl logs <pod-name> -n practice --all-containers=true --tail=100
```

Replace `<pod-name>` with the actual name shown by `kubectl get pods -n practice`.

For a previous container crash:

```powershell
kubectl logs <pod-name> -n practice --previous --tail=100
```

Common status meanings:

- `Running`: the container is running; also check the `READY` column.
- `ImagePullBackOff` / `ErrImagePull`: image name, tag, registry access, or image pull secret problem.
- `CrashLoopBackOff` / `Error`: inspect application logs and configuration.
- `Pending`: inspect pod events, resource availability, and scheduling.
- `CreateContainerConfigError`: check ConfigMaps, Secrets, and environment variable references.

## 5. Access the application

### Option A: Port-forward a Kubernetes Service

Check the actual service names and ports first:

```powershell
kubectl get services -n practice
```

Example for an auth service that listens on port `8082`:

```powershell
kubectl port-forward service/auth-service 8082:8082 -n practice
```

Keep this terminal open. Test from another PowerShell window or Postman at the appropriate local API URL, for example:

```text
http://localhost:8082
```

Use the actual API route, such as `/api/auth/register`, as defined by your controller and gateway configuration. If local port `8082` is already in use, choose another local port:

```powershell
kubectl port-forward service/auth-service 18082:8082 -n practice
```

Then use `http://localhost:18082` locally.

### Option B: Access the frontend

If the frontend is exposed through a Kubernetes Service, inspect its type and ports:

```powershell
kubectl get service -n practice
```

For a ClusterIP frontend service, port-forward using the actual service port:

```powershell
kubectl port-forward service/<frontend-service-name> 5173:<service-port> -n practice
```

Open `http://localhost:5173` if the local port-forward is successful. Replace the placeholders with the actual service name and port. A `LoadBalancer` or `NodePort` service may use a different access method depending on your cluster.

## 6. Troubleshooting

### Pod is not starting

```powershell
kubectl get pods -n practice
kubectl describe pod <pod-name> -n practice
kubectl logs <pod-name> -n practice --all-containers=true --tail=200
```

### Service has no endpoints

```powershell
kubectl get endpoints -n practice
kubectl get pods -n practice --show-labels
kubectl describe service <service-name> -n practice
```

Check that the Service selector matches the pod labels and that the container is ready.

### ImagePullBackOff

```powershell
kubectl describe pod <pod-name> -n practice
```

Verify the image and tag in the Deployment, registry permissions, and `imagePullSecrets`. A locally built image may not be available to a remote cluster.

### Reset Kubernetes resources

Delete only the resources you intend to recreate:

```powershell
kubectl delete -f .\k8s\ -n practice
```

This deletes resources described by those manifests in the `practice` namespace; it does not automatically remove unrelated resources or persistent data. Review the manifests before running it.

## 7. Deploy with Harness

For Harness deployment, make sure:

1. Your Kubernetes cluster is running and reachable from the Harness Delegate.
2. The Delegate is healthy and eligible to execute the task.
3. The Kubernetes connector uses an API URL reachable **from the Delegate**, not necessarily `localhost` on your Windows machine.
4. The connector has the required authentication and RBAC permissions.
5. Your container images are pushed to a registry the cluster can access.
6. The Harness service/environment and manifests reference the correct image names, tags, namespace, and configuration.

Validate cluster access locally first:

```powershell
kubectl cluster-info
kubectl get nodes
kubectl get pods -n practice
```

A successful local `kubectl` command does not guarantee that the Harness Delegate can reach the same cluster. If Harness reports “no eligible delegates available,” check Delegate health, ownership/selection, and connectivity from the Delegate environment.

## Suggested normal workflow

For local Docker Compose:

```powershell
docker compose build
docker compose up -d
docker compose ps
docker compose logs --tail=100
```

For Kubernetes:

```powershell
kubectl cluster-info
kubectl get nodes
kubectl apply -f .\k8s\
kubectl get pods -n practice
kubectl get services -n practice
```

Only proceed to application testing when the relevant pods are ready and the services have endpoints.
