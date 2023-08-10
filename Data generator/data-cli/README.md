# data-cli

## Pre-requisites

- JDK 11

## Build

```
gradlew build
```

## Run 

### Help

```
java -jar build\libs\data-cli-0.0.1-SNAPSHOT.jar generate-billpay-data 
```

### JSON example

```
java -jar build\libs\data-cli-0.0.1-SNAPSHOT.jar generate-billpay-data -u C12032 -b SINGTEL -f P1M -s 2000-01-02 -a 12.22 -o JSON
```

### CSV example

```
java -jar build\libs\data-cli-0.0.1-SNAPSHOT.jar generate-billpay-data -u C12032 -b SINGTEL -f P1M -s 2000-01-02 -a 12.22 -o CSV
```

