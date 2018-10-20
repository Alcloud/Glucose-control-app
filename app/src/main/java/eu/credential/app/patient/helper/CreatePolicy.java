package eu.credential.app.patient.helper;

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import eu.atos.credential.policyhelper.common.PolicyException;
import eu.atos.credential.policyhelper.jackson.PolicyJacksonHelper;
import eu.atos.credential.policyhelper.model.PolicyInfo;
import eu.atos.credential.policyhelper.model.RuleDescription;
import eu.atos.credential.policyhelper.model.RuleInfo;

public class CreatePolicy {
    // set current and deadline times
    private final static int POLICY_DURATION = 1; //Year

    public static String createPolicy(String dataUuid, String authorizedUser) {
        PolicyJacksonHelper jacksonHelper = new PolicyJacksonHelper();

        PolicyInfo policyInfo = new PolicyInfo();

        policyInfo.setVersion("1.0");
        policyInfo.setPolicyDescription("Doctor needs read access to make diabetis diagnosis and write access to return report.");
        policyInfo.setRequestingUser(authorizedUser);

        @SuppressLint("SimpleDateFormat")
        String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                .format(Calendar.getInstance().getTime());
        @SuppressLint("SimpleDateFormat")
        String endOfPolicy = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                .format(setPolicyPeriod(POLICY_DURATION));

        policyInfo.setValidityEndDate(endOfPolicy);
        policyInfo.setValidityStartDate(currentTime);

        List<RuleInfo> ruleInfoList = new ArrayList<>();

        RuleInfo ruleInfo1 = new RuleInfo();
        RuleDescription ruleDescription1 = new RuleDescription();

        ruleInfo1.setResourceId(null);
        ruleInfo1.setScope("READ");

        ruleDescription1.setRequestedName("Personal Health Record");
        ruleDescription1.setRequestedType("FILE");
        ruleDescription1.setRequired("MANDATORY");

        ruleInfo1.setRuleDescription(ruleDescription1);
        ruleInfoList.add(ruleInfo1);

        policyInfo.setRuleInfoList(ruleInfoList);

        policyInfo.getRuleInfoList().get(0).setResourceId(dataUuid);

        try {
            return jacksonHelper.policyInfoToString(policyInfo);
        } catch (PolicyException e) {
            return null;
        }
    }

    private static Date setPolicyPeriod(int n) {
        Calendar c = Calendar.getInstance();
        c.setTime(Calendar.getInstance().getTime());
        c.add(Calendar.YEAR, n);
        return c.getTime();
    }
}
