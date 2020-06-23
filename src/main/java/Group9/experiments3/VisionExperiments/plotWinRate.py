#!/usr/bin/env python

from mpl_toolkits import mplot3d
import numpy as np
import glob
import re
import matplotlib.pyplot as plt
from mpl_toolkits.mplot3d import Axes3D
from scipy import stats
import seaborn as sns


files = glob.glob("testmap*.csv")

win_rates = np.zeros(shape=(len(files), 3))

w_r_index = 0
for i, data_path in enumerate(files):
    match = re.search(r'.*testmap_angle_(?P<angle>\d+)__view_(?P<range>\d+)\.csv', data_path)
    data = np.genfromtxt(data_path, delimiter=',', names=True)
    sns.distplot(data['turnsIntruders'], kde=False)
    win_rate = np.count_nonzero(data['IntrudersWon'] == 1) / data.size
    win_rates[i] = [match.group('angle'), match.group('range'), win_rate]
    break


X = win_rates[:, 0]
Y = win_rates[:, 1]
Z = win_rates[:, 2]



# plt.rcParams.update({'font.size': 20})

# fig = plt.figure()

# ax = fig.add_subplot(111) # projection='3d')
# axp = ax.tricontourf(X, Y, Z,
        # cmap=plt.cm.Spectral, alpha=0.8)

# ax.set_xlabel('View angle (deg)')
# ax.set_ylabel('View range (m)')
# # ax.set_zlabel('Intruder winning rate')

# fig.colorbar(axp, shrink=1, aspect=5).set_label('Intruder winning rate')

plt.show()
