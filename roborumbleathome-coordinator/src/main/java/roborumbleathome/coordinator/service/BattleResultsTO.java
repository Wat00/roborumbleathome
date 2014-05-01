package roborumbleathome.coordinator.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class BattleResultsTO implements Serializable {

    private final String version;

    private final long time;

    private final List<BattleResultTO> battleResultList;

    public BattleResultsTO(String version, long time, List<BattleResultTO> battleResultList) {
	this.version = version;
	this.time = time;
	this.battleResultList = Collections.unmodifiableList(new ArrayList<BattleResultTO>(battleResultList));
    }

    public String getVersion() {
	return version;
    }

    public long getTime() {
	return time;
    }

    public List<BattleResultTO> getBattleResultList() {
	return battleResultList;
    }

}