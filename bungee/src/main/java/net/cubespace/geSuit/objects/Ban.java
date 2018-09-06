package net.cubespace.geSuit.objects;

import java.sql.Timestamp;
import java.util.Date;

public class Ban {
    private int id;
    private String player;
    private String uuid;
    private String ip;
    private String bannedBy;
    private String reason;
    private String type;
    private int active;
    private Date bannedOn;
    private Date bannedUntil;

    public Ban(int id, String player, String uuid, String ip, String bannedBy, String reason, String type, int active, Timestamp timestamp, Timestamp timestamp2) {
        this.id = id;
        this.player = player;
        this.uuid = uuid;
        this.ip = ip;
        this.bannedBy = bannedBy;
        this.reason = reason;
        this.type = type;
        this.bannedOn = timestamp;
        this.bannedUntil = timestamp2;
        this.active = active;
    }

    public String getPlayer() {
        return player;
    }

    public String getBannedBy() {
        return bannedBy;
    }

    public String getReason() {
        return reason;
    }

    public String getType() {
        return type;
    }

	public int getActive() {
		return active;
	}

    public Date getBannedOn() {
        return bannedOn;
    }

    public Date getBannedUntil() {
        return bannedUntil;
    }

    public int getId() {
        return id;
    }

    public String getUuid()
    {
        return uuid;
    }

    public void setUuid(String uuid)
    {
        this.uuid = uuid;
    }

    public String getIp()
    {
        return ip;
    }

}
