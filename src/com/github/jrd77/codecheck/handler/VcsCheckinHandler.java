package com.github.jrd77.codecheck.handler;

import com.github.jrd77.codecheck.data.CheckDataUtils;
import com.github.jrd77.codecheck.data.InterUtil;
import com.github.jrd77.codecheck.data.model.CheckSourceEnum;
import com.github.jrd77.codecheck.data.model.CodeMatchResult;
import com.github.jrd77.codecheck.service.CodeMatchService;
import com.github.jrd77.codecheck.util.CollUtil;
import com.github.jrd77.codecheck.util.HtmlUtil;
import com.github.jrd77.codecheck.util.ResultObject;
import com.github.jrd77.codecheck.vo.CodeMatchContext;
import com.github.jrd77.codecheck.vo.CodeMatchReq;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.checkin.CheckinHandler;
import com.intellij.openapi.vcs.ui.RefreshableOnComponent;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class VcsCheckinHandler extends CheckinHandler {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(VcsCheckinHandler.class.getName());
    private static final String CHECKER_STATE_KEY = "COMMIT_CHECKER_STATE_KEY";
    private final CheckinProjectPanel panel;


    public VcsCheckinHandler(CheckinProjectPanel panel) {
        this.panel = panel;
    }

    public static boolean isCheckMessageEnabled() {
        return PropertiesComponent.getInstance().getBoolean(CHECKER_STATE_KEY, true);
    }

    @Override
    public RefreshableOnComponent getBeforeCheckinConfigurationPanel() {
        final JCheckBox checkBox = new JCheckBox(InterUtil.getValue("show.content.vcs.checkinhandler.name"));

        return new RefreshableOnComponent() {

            @Override
            public JComponent getComponent() {
                JPanel root = new JPanel(new BorderLayout());
                root.add(checkBox, "West");
                return root;
            }

            @Override
            public void refresh() {
            }

            @Override
            public void saveState() {
                PropertiesComponent.getInstance().setValue(CHECKER_STATE_KEY, checkBox.isSelected());
            }

            @Override
            public void restoreState() {
                checkBox.setSelected(isCheckMessageEnabled());
            }
        };
    }



    /**
     * 在提交之前检查
     *
     * @return
     */
    @Override
    public ReturnResult beforeCheckin() {

        logger.info(InterUtil.getValue("logs.common.startcheck"));
        if (!isCheckMessageEnabled()) {
            logger.warning(InterUtil.getValue("logs.validate.notopencheck"));
            return super.beforeCheckin();
        }
        //检查流程发起
        CodeMatchReq codeMatchReq = new CodeMatchReq();
        codeMatchReq.setCheckSource(CheckSourceEnum.TOOL_WINDOW);
        codeMatchReq.setProject(panel.getProject());
        //配置参数
        CodeMatchContext context = CodeMatchService.convertCodeMatchContext(codeMatchReq);
        //检查
        ResultObject<List<CodeMatchResult>> resultObject = CodeMatchService.startCodeMatch(context);
        if (resultObject.getOk() != 0 || CollUtil.isEmpty(resultObject.getData())) {
            return super.beforeCheckin();
        }
        //刷新tool_window
        CheckDataUtils.refreshResultData(resultObject.getData());
        //检查项目中是否有符合规则的提交内容
        final List<CodeMatchResult> cmdList = resultObject.getData();
        if (cmdList != null && cmdList.size() > 0) {
            final String htmlTable = HtmlUtil.buildHtmlTable(cmdList);
            String html = "<html><head>" + UIUtil.getCssFontDeclaration(UIUtil.getLabelFont()) + "</head><body>" +
                    "<br><h3>" + InterUtil.getValue("show.content.vcs.checkinhandler.message.whether") + "</h3>" +
                    "<br>" +
                    htmlTable +
                    "</body></html>";
            int yesOrNo = Messages.showYesNoDialog(html,
                    InterUtil.getValue("show.content.vcs.checkinhandler.message.title"),
                    UIUtil.getErrorIcon());
            return yesOrNo == 0 ? ReturnResult.COMMIT : ReturnResult.CANCEL;
        }
        return ReturnResult.COMMIT;
    }
}
