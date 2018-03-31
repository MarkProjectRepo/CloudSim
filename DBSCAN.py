import numpy as np
import pandas as pd

from sklearn.ensemble import IsolationForest
from sklearn.cluster import DBSCAN
from sklearn import metrics
from sklearn.preprocessing import StandardScaler
from sklearn.decomposition import PCA

import matplotlib.pyplot as plt

X = pd.read_csv('Output.csv')#Read in our data

#Normalize the data, but this may not be necessary as scales matter in this case
X = StandardScaler().fit_transform(X[['Actual CPU', 'Length','Input Size', 'Output Size']])
#X = X[['Actual CPU', 'Length','Input Size', 'Output Size']]
#Perform Principle Component Analysis
pca = PCA(n_components=2)
X = pca.fit_transform(X) # Reduce dimensionality

X = X[np.random.randint(100000, size=25000), :]

db = DBSCAN(eps=0.11, min_samples=25).fit(X)
core_samples_mask = np.zeros_like(db.labels_, dtype=bool)
core_samples_mask[db.core_sample_indices_] = True
labels = db.labels_
n_clusters_ = len(set(labels)) - (1 if -1 in labels else 0)

print(n_clusters_)
# #############################################################################
# Plot result
import matplotlib.pyplot as plt

# Black removed and is used for noise instead.
plt.figure(1)
unique_labels = set(labels)
colors = [plt.cm.Spectral(each)
          for each in np.linspace(0, 1, len(unique_labels))]
plt.subplot(211)
for k, col in zip(unique_labels, colors):
    if k == -1:
        # Black used for noise.
        col = [0, 0, 0, 1]

    class_member_mask = (labels == k)

    xy = X[class_member_mask & core_samples_mask]
    plt.plot(xy[:, 0], xy[:, 1], 'o', markerfacecolor=tuple(col),
             markeredgecolor='k', markersize=14)

    xy = X[class_member_mask & ~core_samples_mask]
    plt.plot(xy[:, 0], xy[:, 1], 'o', markerfacecolor=tuple(col),
             markeredgecolor='k', markersize=6)

plt.title('Estimated number of clusters: %d' % n_clusters_)
plt.subplot(212)
plt.scatter(X[:, 0], X[:, 1], s=1)
print(db.get_params())
plt.show()