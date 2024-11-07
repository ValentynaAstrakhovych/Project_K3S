This directory containes config file fo the k3s cluster run on Oleksii Pasichnyk personal on prem hardware

Use with caution!

To use it
1. find your `kubeconfig` 
2. for ubuntu `/.kube/config`
3. merge or replace content with the file above 
4. to verify run
`kubectl get nodes`
should return something similar

```
k3s-master-2   Ready    control-plane,master   34m   v1.28.6+k3s2
k3s-master-3   Ready    control-plane,master   34m   v1.28.6+k3s2
k3s-master-1   Ready    control-plane,master   34m   v1.28.6+k3s2
k3s-worker-1   Ready    <none>                 11m   v1.28.6+k3s2
k3s-worker-3   Ready    <none>                 11m   v1.28.6+k3s2
k3s-worker-2   Ready    <none>                 11m   v1.28.6+k3s2

```