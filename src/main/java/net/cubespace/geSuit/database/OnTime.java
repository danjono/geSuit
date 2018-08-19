package net.cubespace.geSuit.database;

import net.cubespace.geSuit.geSuit;
import net.cubespace.geSuit.managers.ConfigManager;
import net.cubespace.geSuit.managers.DatabaseManager;
import net.cubespace.geSuit.managers.LoggingManager;
import net.cubespace.geSuit.objects.TimeRecord;
import net.md_5.bungee.api.ChatColor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author geNAZt (fabian.fassbender42@googlemail.com)
 */
public class OnTime implements IRepository {

    public void updatePlayerOnTime(String player, String uuid, long tsStart, long tsEnd) {
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        // Get start/end in calendar objects (required to handle day/month/year changes properly)
        Calendar start = Calendar.getInstance();
        start.setTimeInMillis(tsStart);
        Calendar end = Calendar.getInstance();
        end.setTimeInMillis(tsEnd);
    
        if (geSuit.getInstance().isDebugEnabled())
            geSuit.getInstance().DebugMsg("OnTime (" + player + "): " + sdf.format(new Date(start.getTimeInMillis())) + " -> " + sdf.format(new Date(end.getTimeInMillis())));

        // Set up the initial slots
        Calendar slot = Calendar.getInstance();
        Calendar next   = Calendar.getInstance();
        slot.setTimeInMillis(tsStart);
        slot.set(Calendar.MINUTE, 0);
        slot.set(Calendar.SECOND, 0);
        slot.set(Calendar.MILLISECOND, 0);
        next.setTimeInMillis(slot.getTimeInMillis());
        next.add(Calendar.HOUR, 1);

        int loop = 0;
        List<String> values = new ArrayList<>();

        // Loop through all the hourly time slots that the player was online
        while ((loop == 0) || (end.after(next))) {
        	long time, from, to;
        	if (loop == 0) {
        		from = start.getTimeInMillis();  // On the first loop, measure from the start time, not the beginning of the slot
        	} else {
        		slot.setTimeInMillis(next.getTimeInMillis());
                next.add(Calendar.HOUR, 1);
        		from = slot.getTimeInMillis();  // Every other loop iteration, we measure from the beginning of the slot
        	}

        	if (end.after(next)) {
        		to = next.getTimeInMillis();
        		time = (to - from);  // This is not the last slot, measure time to the end of this slot
        	} else {
        		to = end.getTimeInMillis();
        		time = (to - from);   // This is the last slot, measure time to the "end" time (not the end of the slot)
        	}

        	// Only record positive online times  
        	if (time > 0) {
        		time = (time / 1000);  // Convert from milliseconds to seconds
        
                if (geSuit.getInstance().isDebugEnabled())
                    geSuit.getInstance().DebugMsg("   " +
	        			"Row " + loop + ": " +
	        			"Slot: " + sdf.format(new Date(slot.getTimeInMillis())) + " = " +
	        			sdf.format(new Date(from)) + " -> " +
	        			sdf.format(new Date(to)) + ": " +
	        			time + " secs"
	        	);
	
	        	values.add("('" +uuid+ "', '" +sdf.format(slot.getTimeInMillis())+ "', " +time+ ")");
        	}

        	loop++;

        	if (loop == 100) {
        		LoggingManager.log(ChatColor.RED + "WARNING! OnTime slot checking exceeded 100 loops for " +player+ ", this should never hapen!");
        		break;
        	}
        }
        
        StringBuilder sqlvalues = new StringBuilder();
        for (int x = 0; x < values.size(); x++) {
        	if (x > 0)
        		sqlvalues.append(", ");

        	sqlvalues.append(values.get(x));
        }

        Statement stmt = null;
        try {
        	// Sadly, we can't use prepared statements here because the statement is dynamic
            Connection con = DatabaseManager.connectionPool.getConnection();
            stmt = con.createStatement();
        	stmt.executeUpdate("INSERT DELAYED INTO "+ ConfigManager.main.Table_OnTime + " " +
        			"(uuid,timeslot,time) VALUES " + sqlvalues + " " +
        			"ON DUPLICATE KEY UPDATE time=time+VALUES(time)"
        	);
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
			try {
				if (stmt != null) { stmt.close(); stmt = null; }
			} catch (SQLException e) {
				System.out.println("ERROR: Failed to close SQL Statement!");
				e.printStackTrace();
			}
        }
    }

    public TimeRecord getPlayerOnTime(String uuid) {
        TimeRecord trec = new TimeRecord(uuid);

        try (
                Connection con = DatabaseManager.connectionPool.getConnection();
                Connection con1 = DatabaseManager.connectionPool.getConnection();
                Connection con2 = DatabaseManager.connectionPool.getConnection();
                Connection con3 = DatabaseManager.connectionPool.getConnection();
                Connection con4 = DatabaseManager.connectionPool.getConnection();

                PreparedStatement timeInfo = DatabaseManager.connectionPool.getPreparedStatement("getOnTimeToday", con);
                PreparedStatement timeInfo2 = DatabaseManager.connectionPool.getPreparedStatement("getOnTimeWeek", con1);
                PreparedStatement timeInfo3 = DatabaseManager.connectionPool.getPreparedStatement("getOnTimeMonth", con2);
                PreparedStatement timeInfo4 = DatabaseManager.connectionPool.getPreparedStatement("getOnTimeYear", con3);
                PreparedStatement timeInfo5 = DatabaseManager.connectionPool.getPreparedStatement("getOnTimeTotal", con4)
        ) {
            ResultSet res;
            // Time today
            timeInfo.setString(1, uuid);
            res = timeInfo.executeQuery();
            if (res.next()) trec.setTimeToday(res.getLong(1) * 1000);
            res.close();
        	// Time this week
            timeInfo2.setString(1, uuid);
            res = timeInfo2.executeQuery();
            if (res.next()) trec.setTimeWeek(res.getLong(1) * 1000);
            res.close();

        	// Time this month

            timeInfo3.setString(1, uuid);
            res = timeInfo3.executeQuery();
            if (res.next()) trec.setTimeMonth(res.getLong(1) * 1000);
            res.close();

        	// Time this year
            timeInfo4.setString(1, uuid);
            res = timeInfo4.executeQuery();
            if (res.next()) trec.setTimeYear(res.getLong(1) * 1000);
            res.close();
        	// Total time
            timeInfo5.setString(1, uuid);
            res = timeInfo.executeQuery();
            if (res.next()) trec.setTimeTotal(res.getLong(1) * 1000);
            res.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return trec;
    }    

    public Map<String, Long> getOnTimeTop(int pagenum) {
        LinkedHashMap<String, Long> results = null;
        try (
                Connection con = DatabaseManager.connectionPool.getConnection();
                PreparedStatement top = DatabaseManager.connectionPool.getPreparedStatement("getOnTimeTop", con)
        ) {
            ResultSet res;

            results = new LinkedHashMap<>();
            int offset = (pagenum < 1) ? 0 : (pagenum - 1) * 10;	// Offset = Page number x 10 (but starts at 0 and no less than 0
            top.setInt(1, offset);
            res = top.executeQuery();
            // Build current page of results
            while (res.next()) {
                String name = res.getString("pname");
                Long time = res.getLong("totaltime");
                results.put(name, time);
            }
            res.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return results;
    }

    public Map<Timestamp, Long> getLastLogins(String uuid, int num){
        LinkedHashMap<Timestamp, Long> results = null;
        try (
                Connection con = DatabaseManager.connectionPool.getConnection();
                PreparedStatement lastLogins = DatabaseManager.connectionPool.getPreparedStatement("getLastLogins", con)
        ) {
            lastLogins.setString(1,uuid);
            lastLogins.setInt(2,num);
            results = new LinkedHashMap<>();
            ResultSet res = lastLogins.executeQuery();
            while (res.next()) {
                Timestamp lastlogin = res.getTimestamp("logintime");
                Long ontime = res.getLong("ontime");
                results.put(lastlogin, ontime);
            }
            res.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return results;
    }


    @Override
    public String[] getTable() {
    	return new String[]{ConfigManager.main.Table_OnTime,
    				"`uuid` varchar(32) NOT NULL, "
    			  + "`timeslot` datetime NOT NULL,"
    			  + "`time` int(11) NOT NULL,"
    			  + "UNIQUE KEY `pair` (`uuid`,`timeslot`)"};
    }

    @Override
    public void registerPreparedStatements(ConnectionPool connection) {
        connection.addPreparedStatement("getOnTimeToday", "SELECT SUM(time) FROM "+ ConfigManager.main.Table_OnTime +" ontime WHERE uuid=? AND timeslot >= CURRENT_DATE()");
        connection.addPreparedStatement("getOnTimeWeek",  "SELECT SUM(time) FROM "+ ConfigManager.main.Table_OnTime +" ontime WHERE uuid=? AND timeslot >= STR_TO_DATE(CONCAT(YEARWEEK(NOW()), ' Sunday'), '%X%V %W')");
        connection.addPreparedStatement("getOnTimeMonth", "SELECT SUM(time) FROM "+ ConfigManager.main.Table_OnTime +" ontime WHERE uuid=? AND timeslot >= DATE_FORMAT(NOW(), '%Y-%m-01')");
        connection.addPreparedStatement("getOnTimeYear",  "SELECT SUM(time) FROM "+ ConfigManager.main.Table_OnTime +" ontime WHERE uuid=? AND timeslot > DATE_FORMAT(NOW(), '%Y-01-01')");
        connection.addPreparedStatement("getOnTimeTotal", "SELECT SUM(time) FROM "+ ConfigManager.main.Table_OnTime +" ontime WHERE uuid=?");
        connection.addPreparedStatement("getOnTimeTop",   "SELECT "+ ConfigManager.main.Table_Players +".playername AS pname, "
                 + ConfigManager.main.Table_OnTime +".uuid AS puuid, SUM(time) AS totaltime FROM "
                 + ConfigManager.main.Table_OnTime +" JOIN "+ ConfigManager.main.Table_Players +" ON "
                 + ConfigManager.main.Table_OnTime +".uuid="+ ConfigManager.main.Table_Players +".uuid GROUP BY "
                 + ConfigManager.main.Table_OnTime +".uuid ORDER BY totaltime DESC LIMIT 10 OFFSET ?");
        connection.addPreparedStatement("getLastLogins", "SELECT DATE(timeslot) AS logintime, SUM(time) AS ontime FROM "
                 + ConfigManager.main.Table_OnTime +" WHERE uuid = ? GROUP BY DATE(`ontime`.timeslot) ORDER BY timeslot DESC LIMIT ?;");
       }

	@Override
	public void checkUpdate() {
	}
}
