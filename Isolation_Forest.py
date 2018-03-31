import numpy as np
import pandas as pd

from sklearn.ensemble import IsolationForest
from sklearn.cluster import DBSCAN
from sklearn.preprocessing import StandardScaler
from sklearn.decomposition import PCA

import matplotlib.pyplot as plt

rng = np.random.RandomState(42)

X = pd.read_csv('Output.csv')#Read in our data

#Normalize the data, but this may not be necessary as scales matter in this case
X = StandardScaler().fit_transform(X[['Actual CPU', 'Length','Input Size', 'Output Size']])

#Perform Principle Component Analysis
pca = PCA(n_components=2)
X = pca.fit_transform(X) # Reduce dimensionality

#Initialize and fit the forest to the data
isof = IsolationForest().fit(X)

#Set the range of the graph
xx, yy = np.meshgrid(np.linspace(-5, 5, 50), np.linspace(-5, 5, 50))
Z = isof.decision_function(np.c_[xx.ravel(), yy.ravel()])
Z = Z.reshape(xx.shape)#Finish initializing the contour function of the isolation forest

plt.contourf(xx, yy, Z, cmap=plt.cm.Blues_r)#Plot the contour function
plt.contour(xx, yy, Z, linewidths=2, colors='red')
b1 = plt.scatter(X[:, 0], X[:, 1], c='yellow', s=5, alpha=0.09)#Plot the data on top of the contour

plt.title("Isolation Forest")

plt.show()#Display the graph