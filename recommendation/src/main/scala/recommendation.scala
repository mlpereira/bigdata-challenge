import org.apache.spark.sql._
import java.io._
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.commons.io.FileUtils
import org.apache.commons.io.filefilter.WildcardFileFilter


object Recommendation {
  def main(args: Array[String]) {
    val conf = new SparkConf().setMaster("local[*]").setAppName("Recommendation")

    val sc = SparkContext.getOrCreate(conf)
    val sqlContext = new SQLContext(sc)
    import sqlContext.implicits._

    while(true) {
      val names = new File("../api/parquet/").list

      val first = names.headOption
      if (first != None) {
        val newDataDF = sqlContext.read.parquet("../api/parquet/" + first.get)        // read back parquet to DF
        newDataDF.show()
        FileUtils.deleteDirectory(new File("../api/parquet/" + first.get))
      } else {
        Thread.sleep(10000)
      }
    }
  }
}
