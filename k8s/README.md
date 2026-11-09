# Running services on Kubernetes (Docker Desktop)

A hybrid local setup for learning Kubernetes without running out of memory:

- **In the cluster:** api-gateway, auth-service, inventory-service, cart-service, order-service.
- **On the host, as before:** the databases, Kafka and Redis (docker compose) and the other 13 services (`mvn spring-boot:run`).

Pods reach the host through `host.docker.internal`. Docker Desktop publishes the five services on `localhost:8080`–`8084`, so the Angular app and the host-run services keep working unchanged.

## Layout

```text
Dockerfile                          one Dockerfile for every service (--build-arg SERVICE=...)
k8s/
  build-images.ps1                  builds eshop/<service>:0.1 for the five services
  base/                             environment-neutral manifests
    kustomization.yaml              ConfigMap "platform", Secret "credentials", image tags
    inventory-service.yaml          reference Deployment + Service, every field commented
    auth / cart / order-service.yaml, api-gateway.yaml
  overlays/docker-desktop/
    kustomization.yaml              host.docker.internal addresses, LoadBalancer Services
```

## 1. One-time setup

1. Docker Desktop → **Settings → Kubernetes** → **Enable Kubernetes**, cluster type **kubeadm**, then **Apply & restart**. Images you build locally are then visible to the cluster with no registry.
2. Check:

   ```powershell
   kubectl config use-context docker-desktop
   kubectl get nodes          # one node, STATUS Ready
   ```

## 2. Start the infrastructure

```powershell
docker compose -f infrastructure/docker/docker-compose.yml up -d
```

Kafka now also listens on `9093`, advertised as `host.docker.internal:9093` for pods. If Kafka was already running, the command above recreates it with the new listener.

## 3. Build the images

```powershell
powershell -ExecutionPolicy Bypass -File k8s\build-images.ps1
```

The script runs `mvn package` on the host (reusing your `~/.m2`), then builds a small runtime image that just copies the jar in. To build only some services: `-Services order-service,cart-service`.

## 4. Deploy

Stop the host-run copies of the five services first, or their ports will clash with the LoadBalancer Services.

```powershell
kubectl apply -k k8s/overlays/docker-desktop
kubectl -n eshop get pods -w            # wait for READY 1/1 on all five (Ctrl+C to stop watching)
```

Then start the other services on the host as usual, and the frontend with `npm start`. Open http://localhost:4200.

## 5. Look around

| What | Command |
|---|---|
| Everything in the namespace | `kubectl -n eshop get all` |
| Logs (follow) | `kubectl -n eshop logs deploy/order-service -f` |
| Why a pod isn't starting | `kubectl -n eshop describe pod -l app=order-service` |
| Env vars a pod actually got | `kubectl -n eshop exec deploy/order-service -- env` |
| Shell inside a pod | `kubectl -n eshop exec -it deploy/order-service -- sh` |
| Self-healing | `kubectl -n eshop delete pod -l app=cart-service`, then watch a new one start |
| Scale | `kubectl -n eshop scale deploy/inventory-service --replicas=2` |
| Restart after rebuilding the same tag | `kubectl -n eshop rollout restart deploy/inventory-service` |
| See the YAML kustomize produces | `kubectl kustomize k8s/overlays/docker-desktop` |
| Remove everything | `kubectl delete -k k8s/overlays/docker-desktop` |

## Shipping a change (rolling update)

1. Build with a new tag: `k8s\build-images.ps1 -Tag 0.2 -Services order-service`
2. Change `newTag` for `eshop/order-service` in `k8s/base/kustomization.yaml` to `"0.2"`.
3. `kubectl apply -k k8s/overlays/docker-desktop` and watch `kubectl -n eshop rollout status deploy/order-service`.
4. Changed your mind? `kubectl -n eshop rollout undo deploy/order-service`.

## How configuration reaches Spring

Spring Boot reads environment variables as properties: `SPRING_DATASOURCE_URL` sets `spring.datasource.url`, `ORDER_SERVICE_BASE_URL` sets `order-service.base-url`. So nothing in `application.*` changes. The `platform` ConfigMap holds hosts and service URLs, the `credentials` Secret holds passwords, and each Deployment combines them, for example `jdbc:postgresql://$(POSTGRES_HOST):5432/inventory_db`.

The gateway's routes read the same `*_SERVICE_BASE_URL` variables, with the old `localhost` URLs as defaults for running outside Kubernetes.

## Troubleshooting

| Symptom | Likely cause |
|---|---|
| `ErrImageNeverPull` / `ImagePullBackOff` | Image not built, or the tag doesn't match `kustomization.yaml`. Run `docker images eshop/*`. |
| `CrashLoopBackOff` | Read `kubectl -n eshop logs <pod> --previous`. Usually a database or Kafka that isn't up. |
| `OOMKilled` in `describe pod` | Raise `limits.memory` in that service's YAML (heap is 60% of the limit). |
| Pod running but `READY 0/1` | Readiness probe failing; a dependency such as the database is unreachable. |
| `Service ... pending` external IP | Another process already uses that port on localhost (often the host-run copy of the service). |

## Next steps

1. Add the next services: copy `cart-service.yaml`, change name, port and database, add it to `base/kustomization.yaml` and `build-images.ps1`, and remove its `host.docker.internal` line from the overlay.
2. Put the Angular app in the cluster (nginx image) behind an Ingress, and turn the Services back into ClusterIP.
3. Run Postgres, Kafka and Redis in the cluster with StatefulSets or Helm charts.
4. Add an overlay for a cloud cluster (AKS, EKS or GKE) with a container registry and managed databases.
