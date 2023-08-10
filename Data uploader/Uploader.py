import json

f = open("Data generator/Data/file.json")

data = json.load(f)

for i in data:
    print(i)

index = "data"
id = 1

writeData = []

with open("Data generator/Data/WriteData.json", "w") as write_file:
    for i in range(len(data)):
        print("{\"index\": {\"_index\": \"data\", \"_id\": " + str(id) + "}}", file=write_file)
        if "\'" in str(data[i]):
            print(str(data[i]).replace("\'", "\""), file=write_file)
        id += 1
