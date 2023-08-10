import pandas as pd
import plotly.graph_objects as go
from sklearn import metrics
from sklearn.model_selection import train_test_split, GridSearchCV
from sklearn.neighbors import KNeighborsClassifier
from sklearn.ensemble import RandomForestClassifier
from datetime import date
import datetime
from dateutil import parser
from dateutil.relativedelta import relativedelta

import json

f = open("Data generator/Data/file.json")

data = json.load(f)

dates = []



for i in range(len(data)):
    k= parser.parse(data[i]['date'])
    date = datetime.datetime.strftime(k, '%Y-%m-%d')
    dates.append(date)

mldata = pd.DataFrame({"Date": dates})

mldata['Date'] = pd.to_datetime(mldata['Date'])
mldata['Purchase'] = 1

daterange = pd.date_range(start=mldata['Date'].min(), end=mldata['Date'].max())
mldata = mldata.set_index('Date').reindex(daterange).fillna(0.0).rename_axis('Date').reset_index()

print(mldata)

x_train, x_test, y_train, y_test = train_test_split(mldata.drop(['Purchase'], axis=1), mldata['Purchase'],test_size=0.3, random_state=1, shuffle=False)

trace_specs = [
    [x_train, y_train, '0', 'Train', 'square'],
    [x_train, y_train, '1', 'Train', 'circle'],
    [x_test, y_test, '0', 'Test', 'square-dot'],
    [x_test, y_test, '1', 'Test', 'circle-dot']
]

fig = go.Figure(data=[
    go.Scatter(
        x=X[y==label, 0], y=X[y==label, 1],
        name=f'{split} Split, Label {label}',
        mode='markers', marker_symbol=marker
    )
    for X, y, label, split, marker in trace_specs
])
fig.update_traces(
    marker_size=12, marker_line_width=1.5,
    marker_color="lightyellow"
)
fig.show()


knn = KNeighborsClassifier(n_neighbors = 3, algorithm = 'auto', weights = 'distance') 
knn.fit(x_train, y_train)  
knn_prediction = knn.predict(x_test)

knn_matrix = metrics.confusion_matrix(knn_prediction, y_test)
print(f"""
Confusion matrix for KNN model:
TN:{knn_matrix[0][0]}    FN:{knn_matrix[0][1]}
FP:{knn_matrix[1][0]}    TP:{knn_matrix[1][1]}""")
