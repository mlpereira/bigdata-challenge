import org.apache.spark.sql._
import java.io._
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.commons.io.FileUtils
import org.apache.commons.io.filefilter.WildcardFileFilter


object ViewsPerUrl {
  def main(args: Array[String]) {
    val conf = new SparkConf().setMaster("local[*]").setAppName("ViewsPerUrl")

    val sc = SparkContext.getOrCreate(conf)
    val sqlContext = new SQLContext(sc)
    import sqlContext.implicits._

    while(true) {
      val names = new File("../api/consumer/url-user-parquets/").list

      val first = names.headOption
      if (first != None) {
        val newDataDF = sqlContext.read.parquet("../api/consumer/url-user-parquets/" + first.get)        // read back parquet to DF
        newDataDF.show()
        newDataDF.rdd.collect.foreach { row =>
          val urls = new File("./url-parquets/").list
          var list : List[(String,String)] = List()
          list = list:+((row(1).toString, ""))
          val rdd = sc.parallelize(list)
          val dataframe = rdd.toDF

          if (urls.contains(row(0))) {
            val newUrlsDF = sqlContext.read.parquet("./url-parquets/" + row(0).toString)
            val finalDF = dataframe.union(newUrlsDF).distinct().persist()
            finalDF.show()
            FileUtils.deleteDirectory(new File("./url-parquets/" + row(0).toString))
            finalDF.write.parquet("./url-parquets/" + row(0).toString)
            finalDF.show()
          } else {
            dataframe.write.parquet("./url-parquets/" + row(0).toString)
          }
        }
        FileUtils.deleteDirectory(new File("../api/consumer/url-user-parquets/" + first.get))
      } else {
        Thread.sleep(10000)
      }
    }
  }
}
