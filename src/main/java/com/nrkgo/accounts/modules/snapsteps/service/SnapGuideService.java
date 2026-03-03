package com.nrkgo.accounts.modules.snapsteps.service;

import com.nrkgo.accounts.modules.snapsteps.dto.SnapGuideDto;
import com.nrkgo.accounts.modules.snapsteps.model.SnapGuide;
import com.nrkgo.accounts.model.User;
import java.util.List;

public interface SnapGuideService {
    SnapGuide saveGuide(SnapGuideDto guideDto, User user);

    List<SnapGuide> getGuidesForUser(User user);

    SnapGuide getGuideById(String id, User user);

    void deleteGuide(String id, User user);
}
