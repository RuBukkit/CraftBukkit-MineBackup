package ltguide.minebackup.utils

import ltguide.minebackup.Debug
import ltguide.minebackup.MineBackup
import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.util.*

/**
 * Project: CraftBukkit-MineBackup
 * Created by Markus on 16.07.2016.
 */
class MySqlUtils(private val plugin: MineBackup) {

    private val hostname: String
    private val port: Int
    private val database: String
    private val user: String
    private val password: String

    init {
        val config = plugin.config

        hostname = config.getString("mysql.hostname")
        port = config.getInt("mysql.port")
        database = config.getString("mysql.database")
        user = config.getString("mysql.user")
        password = config.getString("mysql.password")
    }

    @Throws(SQLException::class)
    fun exportDatabase(connection: Connection, saveFolder: File) {
        val tableNames = getListOfTables(connection)

        tableNames.forEach {
            var filename = saveFolder.absolutePath + "/$it.csv"
            filename = filename.replace('\\', File.separatorChar).replace('/', File.separatorChar).replace("\\", "\\\\")
            val selectQuery = "SELECT * FROM $it INTO OUTFILE '$filename' FIELDS TERMINATED BY ',' ENCLOSED BY '\"' LINES TERMINATED BY '\n'"
            val statement = connection.prepareStatement(selectQuery)
            statement.execute()

            if (Debug.ON) plugin.logger.info("Export table $it")
        }
    }

    @Throws(SQLException::class)
    fun connectToDatabase(): Connection {
        val connProps = Properties()
        connProps.put("user", user)
        connProps.put("password", password)
        val connection = DriverManager.getConnection("jdbc:mysql://$hostname:$port/$database", connProps)

        if (Debug.ON) plugin.logger.info("Connected to $hostname:$port/$database")

        return connection
    }

    @Throws(SQLException::class)
    fun closeDatabase(connection: Connection) {
        connection.close()
    }

    private fun getListOfTables(connection: Connection): List<String> {
        val metaData = connection.metaData
        val tablesSet = metaData.getTables(connection.catalog, null, "", null)

        val tablesList = ArrayList<String>()
        while (tablesSet.next()) {
            tablesList.add(tablesSet.getString("TABLE_NAME"))
        }

        return tablesList
    }
}