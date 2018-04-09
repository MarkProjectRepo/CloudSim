import numpy as np
import pandas as pd

from sklearn.ensemble import IsolationForest
from sklearn.cluster import DBSCAN
from sklearn import metrics
from sklearn.preprocessing import StandardScaler
from sklearn.decomposition import PCA

X = np.array(pd.read_csv('Output.csv')[['Actual CPU', 'Length','Input Size', 'Output Size']])#Read in our data
Y = pd.read_csv('OutputNoisyOnly.csv')[['Actual CPU', 'Length','Input Size', 'Output Size']]
#X = X[np.random.randint(100000, size=25000), :]
#Normalize the data, but this may not be necessary as scales matter in this case

XY = StandardScaler().fit_transform(np.vstack((X, Y)))

#Perform Principle Component Analysis
pca = PCA(n_components=2)
XY = pca.fit_transform(XY) # Reduce dimensionality
#Select a random subset of 25,000 chose this to cope with memory errors from DBSCAN
#X = X[np.random.randint(100000, size=25000), :]

#Initialize the dbscan fit 
#eps is the minimum distance to be considered similar/same
#min_samples is the number of elements close to a point for it to be considered 'central'
print(XY.shape)
db = DBSCAN(eps=0.07, min_samples=19).fit(XY)

#Init an array of the same shape as the labels, then assign it True values 
core_samples_mask = np.zeros_like(db.labels_, dtype=bool)
core_samples_mask[db.core_sample_indices_] = True
labels = db.labels_
n_clusters_ = len(set(labels)) - (1 if -1 in labels else 0)

print(n_clusters_)
# #############################################################################
# Plot results, all taken from standard graphing procedures online
import matplotlib.pyplot as plt

# Black removed and is used for noise instead.
plt.figure(1)
unique_labels = set(labels)
y_labels = np.ones([XY.shape[0], 1])
y_labels[100000:] *= -1
colors = [plt.cm.Spectral(each)
          for each in np.linspace(0, 1, len(unique_labels))]
plt.subplot(211)
for k, col in zip(unique_labels, colors):
    if k == -1:
        # Black used for noise.
        col = [0, 0, 0, 1]

    class_member_mask = (labels == k)

    xy = XY[class_member_mask & core_samples_mask]
    plt.plot(xy[:, 0], xy[:, 1], 'o', markerfacecolor=tuple(col),
             markeredgecolor='k', markersize=14)

    xy = XY[class_member_mask & ~core_samples_mask]
    plt.plot(xy[:, 0], xy[:, 1], 'o', markerfacecolor=tuple(col),
             markeredgecolor='k', markersize=6)

plt.title('Estimated number of clusters: %d' % n_clusters_)
plt.subplot(212)
plt.scatter(XY[:99999, 0], XY[:99999, 1], s=1, c='blue')
plt.scatter(XY[99999:, 0], XY[99999:, 1], s=1, c='red')
print(db.get_params())
plt.show()