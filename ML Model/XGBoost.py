import pandas as pd
import plotly.graph_objects as go
import numpy as np
from sklearn import metrics
from sklearn.model_selection import train_test_split, GridSearchCV
from datetime import date
import datetime
from dateutil import parser
import xgboost as xgb
from dateutil.relativedelta import relativedelta
from sklearn.ensemble import RandomForestClassifier

import json

f = open("Data generator/Data/file.json")

data = json.load(f)

dates = []

for i in range(len(data)):
    k= parser.parse(data[i]['date'])
    date = datetime.datetime.strftime(k, '%Y-%m-%d')
    print(date)
    dates.append(date)

mldata = pd.DataFrame({"Date": dates})

mldata['Date'] = pd.to_datetime(mldata['Date'])
mldata['Purchase'] = 1

daterange = pd.date_range(start=mldata['Date'].min(), end=mldata['Date'].max())
mldata = mldata.set_index('Date').reindex(daterange).fillna(0.0).rename_axis('Date').reset_index()

print(mldata)

mldata['Month'] = mldata['Date'].dt.month
mldata['Day'] = mldata['Date'].dt.day
mldata['Workday_N'] = np.busday_count(
                    mldata['Date'].values.astype('datetime64[M]'),
                    mldata['Date'].values.astype('datetime64[D]'))
mldata['Week_day'] = mldata['Date'].dt.weekday
mldata['Week_of_month'] = (mldata['Date'].dt.day 
                         - mldata['Date'].dt.weekday - 2) // 7 + 2
mldata['Weekday_order'] = (mldata['Date'].dt.day + 6) // 7
mldata = mldata.set_index('Date')

x_train, x_test, y_train, y_test = train_test_split(mldata.drop(['Purchase'], axis=1), mldata['Purchase'],test_size=0.3, random_state=1, shuffle=False)

random_forest = RandomForestClassifier(n_estimators=50,
                                       max_depth=10, random_state=1)
random_forest.fit(x_train, y_train)
rf_prediction = random_forest.predict(x_test)

rf_matrix = metrics.confusion_matrix(rf_prediction, y_test)
print(f"""
Confusion matrix for Random Forest model:
TN:{rf_matrix[0][0]}    FN:{rf_matrix[0][1]}
FP:{rf_matrix[1][0]}    TP:{rf_matrix[1][1]}""")

x_predict = pd.DataFrame(pd.date_range(date.today(), (date.today() +
            relativedelta(years=1)),freq='d'), columns=['Date'])
x_predict['Day'] = x_predict['Date'].dt.day
x_predict['Workday_N'] = np.busday_count(
                x_predict['Date'].values.astype('datetime64[M]'),
                x_predict['Date'].values.astype('datetime64[D]'))
x_predict['Week_day'] = x_predict['Date'].dt.weekday
x_predict['Week_of_month'] = (x_predict['Date'].dt.day - 
                              x_predict['Date'].dt.weekday - 2)//7+2
x_predict['Weekday_order'] = (x_predict['Date'].dt.day + 6) // 7
x_predict['Month'] = x_predict['Date'].dt.month
x_predict = x_predict.set_index('Date')
prediction = random_forest.predict(x_predict)