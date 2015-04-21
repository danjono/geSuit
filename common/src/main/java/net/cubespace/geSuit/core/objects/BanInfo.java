package net.cubespace.geSuit.core.objects;

import java.util.Map;
import java.util.UUID;

import net.cubespace.geSuit.core.storage.Storable;
import net.cubespace.geSuit.core.util.Utilities;

public class BanInfo<T> implements Storable {
    private T who;
    private long date;
    private long until;
    private String reason;
    private String bannedBy;
    private UUID bannedById;
    
    public BanInfo(T who) {
        this.who = who;
    }
    
    public T getWho() {
        return who;
    }
    
    public long getDate() {
        return date;
    }
    
    public long getUntil() {
        return until;
    }
    
    public boolean isTemporary() {
        return until != 0;
    }
    
    public String getReason() {
        return reason;
    }
    
    public String getBannedBy() {
        return bannedBy;
    }
    
    public UUID getBannedById() {
        return bannedById;
    }
    
    @Override
    public void load(Map<String, String> values) {
        date = Utilities.parseDate(values.get("date"));
        reason = values.get("reason");
        bannedBy = values.get("banned-by");
        if (values.containsKey("banned-by-id")) {
            bannedById = Utilities.makeUUID(values.get("banned-by-id"));
        } else {
            bannedById = null;
        }
        
        if (values.containsKey("until")) {
            until = Utilities.parseDate(values.get("until"));
        } else {
            until = 0;
        }
    }
    
    @Override
    public void save(Map<String, String> values) {
        values.put("date", Utilities.formatDate(date));
        values.put("reason", reason);
        values.put("banned-by", bannedBy);
        if (bannedById != null) {
            values.put("banned-by-id", Utilities.toString(bannedById));
        }
        
        if (until != 0) {
            values.put("until", Utilities.formatDate(until));
        }
    }
}