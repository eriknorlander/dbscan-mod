# dbscan-spark
This is an application to cluster geographical data from a large csv file, producing a mean and covariance matrix of that cluster to a json file. The clustering is done by a modified DBSCAN-algorithm, called DBSCAN_MOD in this project. Originally, this was made as a summer project at a company called Combain, but this is a bare-bones scaled down version.

## Quick Start

1. Install Scala, Apache Spark and SBT:
```
$ brew install scala
$ brew install apache-spark
$ brew install sbt    
```

2. Package job as jar:
```
$ sbt package
```
3. Submit job locally:
```
$ spark-submit --class "Distributer" --master local[2] /Users/eriknorlander/dbscan-spark/target/scala-2.11/dbscan_2.11-1.0.jar /Users/eriknorlander/dbscan-spark/input/query_result.csv /Users/eriknorlander/dbscan-spark/output/result.json 0.01 10 ""
```
4. If classNotFoundException:
```
export SPARK_DIST_CLASSPATH=/Users/eriknorlander/Distributer/target/scala-2.11/classes:$SPARK_DIST_CLASSPATH
```
## Run on AWS
1. Install awscli in any way you wish:
https://docs.aws.amazon.com/cli/latest/userguide/cli-install-macos.html

2. Configure it for a user of your AWS-account, needing:
* Access key ID
* Secret access key
Usually by creating a new IAM-user.

### Start a dummy-cluster:
This is simply included for you to try your connection.
```
$ aws emr create-cluster \
    --name "1-node dummy cluster" \
    --instance-type m3.xlarge \
    --release-label emr-4.1.0 \
    --instance-count 1 \
    --use-default-roles \
    --applications Name=Spark \
    --auto-terminate
```
### Start a real spark job
1. Create fat jars using sbt-assembly:
This is installed automatically if this repo is cloned. If not, create a file
```
~/Distributer/project/target/assembly.sbt
```
and write the following (or any more recent version)
```
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.7")
```
After this is done, you can create a fat jar (with all dependencies) by typing
```
$ sbt assembly
```
This will create a file which we will use in the next step.
2. Upload jar to a S3 bucket of your choice:
```
$ aws s3 cp /Users/eriknorlander/dbscan-spark/target/scala-2.11/DBSCAN-assembly-1.0.jar s3://dbscan-spark/jars/
```
3. Write a file step.json on the format below. Here the "Args" are the argument
have as input to your main method. In this case, I take the input from one S3-
bucket, dump the output to another S3-bucket, use epsilon = 200 and minPts = 20.
```
[
  {
    "Name": "DBSCAN",
    "Type":"CUSTOM_JAR",
    "Jar":"s3://dbscan-spark/jars/dbscan_2.11-1.0.jar",
    "Args":
    [
      "s3://dbscan-spark/input/input.csv",
      "s3://dbscan-spark/output",
      "10",
      "100"
    ],
    "ActionOnFailure": "TERMINATE_CLUSTER"
  },
  {
    ...
  }
]
```
4. Create a cluster and run the step:
```
$ aws emr create-cluster \
    --name "DBSCAN with some jobs to get the day started" \
    --release-label emr-5.16.0 \
    --log-uri s3://dbscan-spark/logs/ \
    --enable-debugging \
    --use-default-roles \
    --applications Name=Spark \
    --instance-type r3.xlarge \
    --instance-count 3 \
    --steps file://step.json \
    --auto-terminate
```
or adding a step to an already running cluster:
```
spark-submit \ --class "Distributer" \ --master "yarn" \ /Users/eriknorlander/Distributer/target/scala-2.11/DBSCAN-assembly-1.0.jar s3://dbscan-spark/input/swe_1k_10k.csv s3://dbscan-spark/output 200 20
```
