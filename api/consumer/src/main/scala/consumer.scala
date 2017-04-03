import org.apache.spark.sql._
import org.apache.spark.sql.SparkSession
import org.apache.spark.SparkConf
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.Seconds
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.log4j.{Level, Logger}


object Consumer {
  def main(args: Array[String]) {
    val conf = new SparkConf().setMaster("local[*]").setAppName("KafkaReceiver")
    val ssc = new StreamingContext(conf, Seconds(10))

    val kafkaStream = KafkaUtils.createStream(ssc, "localhost:2181","spark-streaming-consumer-group", Map("test" -> 5))
    kafkaStream.foreachRDD { rdd =>
      val timestamp: Long = System.currentTimeMillis /1000
      val timestring: String = timestamp.toString

      val spark = SparkSession.builder.config(rdd.sparkContext.getConf).getOrCreate()
      import spark.implicits._
      val rootLogger = Logger.getRootLogger()
      rootLogger.setLevel(Level.ERROR)

      if (!rdd.isEmpty) {
        val dataframe = rdd.toDF
        dataframe.show()
        dataframe.write.parquet("./url-user-parquets/" + timestring)
      }

    }
    ssc.start()
    ssc.awaitTermination()
  }
}
