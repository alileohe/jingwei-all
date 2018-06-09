package com.taobao.jingwei.webconsole.web.module.screen.api;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.Navigator;
import com.alibaba.citrus.turbine.dataresolver.Param;
import com.alibaba.common.lang.StringUtil;
import com.taobao.jingwei.common.JingWeiConstants;
import com.taobao.jingwei.webconsole.biz.ao.JingweiGroupAO;
import com.taobao.jingwei.webconsole.biz.ao.JingweiServerAO;
import com.taobao.jingwei.webconsole.biz.ao.JingweiTaskAO;
import com.taobao.jingwei.webconsole.biz.dao.JwPermissionDao;
import com.taobao.jingwei.webconsole.biz.dao.JwResourceDao;
import com.taobao.jingwei.webconsole.biz.dao.JwUserRoleDao;
import com.taobao.jingwei.webconsole.biz.manager.JingweiZkConfigManager;
import com.taobao.jingwei.webconsole.util.ConsoleServerHosts;
import com.taobao.jingwei.webconsole.util.position.MasterPositionHelper;
import com.taobao.jingwei.webconsole.util.upload.UploadFileService;
import com.taobao.jingwei.webconsole.web.module.screen.api.cmd.ApiCommand;
import com.taobao.jingwei.webconsole.web.module.screen.api.cmd.BatchUpdateMysqlBinlogPosCommand;
import com.taobao.jingwei.webconsole.web.module.screen.api.cmd.CopyTar2BakCommand;
import com.taobao.jingwei.webconsole.web.module.screen.api.cmd.DeleteBackTarCommand;
import com.taobao.jingwei.webconsole.web.module.screen.api.cmd.DeletePermissionCommand;
import com.taobao.jingwei.webconsole.web.module.screen.api.cmd.DeleteServerTarCommand;
import com.taobao.jingwei.webconsole.web.module.screen.api.cmd.DeleteTarCommand;
import com.taobao.jingwei.webconsole.web.module.screen.api.cmd.GetBakTarCommand;
import com.taobao.jingwei.webconsole.web.module.screen.api.cmd.GetBinlogPosByTddlGroupCommand;
import com.taobao.jingwei.webconsole.web.module.screen.api.cmd.GetConsoleTarCommand;
import com.taobao.jingwei.webconsole.web.module.screen.api.cmd.GetConsoleTarTimeCommand;
import com.taobao.jingwei.webconsole.web.module.screen.api.cmd.GetGroupsCommand;
import com.taobao.jingwei.webconsole.web.module.screen.api.cmd.GetLocalTarCommand;
import com.taobao.jingwei.webconsole.web.module.screen.api.cmd.GetMysqlBinlogPosByTaskName;
import com.taobao.jingwei.webconsole.web.module.screen.api.cmd.GetServerTarCommand;
import com.taobao.jingwei.webconsole.web.module.screen.api.cmd.GetServersCommand;
import com.taobao.jingwei.webconsole.web.module.screen.api.cmd.GetTasksCommand;
import com.taobao.jingwei.webconsole.web.module.screen.api.cmd.JwUserRoleCommand;
import com.taobao.jingwei.webconsole.web.module.screen.api.cmd.RevertTarCommand;
import com.taobao.jingwei.webconsole.web.module.screen.api.cmd.SaveResourcesCommand;
import com.taobao.jingwei.webconsole.web.module.screen.api.cmd.ToggleGroupBatchCommand;
import com.taobao.jingwei.webconsole.web.module.screen.api.cmd.ToggleTaskUnderGroupCommand;
import com.taobao.jingwei.webconsole.web.module.screen.api.cmd.UpdateMysqlBinlogPosCommand;

public class JingweiGateWay implements JingWeiConstants {
	private static final Log log = LogFactory.getLog(JingweiGateWay.class);

	/** 获取本地的 /jingwei/uploads/tars/目录下的所有tar文件 */
	public static final String GET_LOCAL_TARS = "getLocalTars";

	@Autowired
	private HttpServletRequest request;

	@Autowired
	private HttpServletResponse response;

	@Autowired
	private JingweiServerAO jwServerAO;

	@Autowired
	private JingweiTaskAO jwTaskAO;

	@Autowired
	private JingweiGroupAO jwGroupAO;

	@Autowired
	private UploadFileService uploadFileService;

	@Autowired
	public ConsoleServerHosts consoleServerHosts;

	@Autowired
	private MasterPositionHelper positionHelper;

	@Autowired
	private JingweiZkConfigManager jwConfigManager;

	@Autowired
	private JwPermissionDao jwPermissionDao;

	@Autowired
	private JwResourceDao jwResourceDao;

	@Autowired
	private JwUserRoleDao jwUserRoleDao;

	public void execute(Context context, Navigator navigator, @Param(name = "act") String act) throws IOException {
		ApiCommand command = null;

		log.warn(request.getRequestURI());

		if (StringUtil.isBlank(act)) {
			JsonUtil.writeFailJson2Client(response);
			return;
		}

		if (act.equalsIgnoreCase(GET_LOCAL_TARS)) {
			command = new GetLocalTarCommand(request, response);
			((GetLocalTarCommand) command).setUploadFileService(uploadFileService);
		}

		if (act.equalsIgnoreCase(GetServerTarCommand.CMD_STR)) {
			// http://10.13.43.86/jingwei/api/JingweiGateWay.do?act=getLocalTars&hostName=balabala
			command = new GetServerTarCommand(request, response, jwServerAO);
		}
		if (act.equalsIgnoreCase(DeleteTarCommand.CMD_STR)) {
			// http://10.13.43.86/jingwei/api/JingweiGateWay.do?act=deleteTar&tarName=balbabala&targetConsoleIp=XXX
			command = new DeleteTarCommand(request, response);
			((DeleteTarCommand) command).setConsoleServerHosts(consoleServerHosts);
		}
		if (act.equalsIgnoreCase(GetConsoleTarCommand.CMD_STR)) {
			// 向server发送指令，server请求console的tar
			command = new GetConsoleTarCommand(request, response, jwServerAO);
			((GetConsoleTarCommand) command).setConsoleServerHosts(consoleServerHosts);
		}
		if (act.equalsIgnoreCase(DeleteServerTarCommand.CMD_STR)) {
			// http://10.13.43.86/jingwei/api/JingweiGateWay.do?act=deleteServerTar&tarName=balbabala&hostName
			command = new DeleteServerTarCommand(request, response, jwServerAO);
		}
		if (act.equalsIgnoreCase(GetBakTarCommand.CMD_STR)) {
			// http://10.13.43.86/jingwei/api/JingweiGateWay.do?act=getBakTar&hostName=xx
			command = new GetBakTarCommand(request, response, jwServerAO);
		}
		if (act.equalsIgnoreCase(CopyTar2BakCommand.CMD_STR)) {
			// http://10.13.43.86/jingwei/api/JingweiGateWay.do?act=copyTar2Bak&hostName=xx&tarName=XX
			command = new CopyTar2BakCommand(request, response, jwServerAO);
		}
		if (act.equalsIgnoreCase(DeleteBackTarCommand.CMD_STR)) {
			// http://10.13.43.86/jingwei/api/JingweiGateWay.do?act=deleteBackTar&hostName=xx&tarName=XX
			command = new DeleteBackTarCommand(request, response, jwServerAO);
		}
		if (act.equalsIgnoreCase(GetBinlogPosByTddlGroupCommand.CMD_STR)) {
			// http://10.13.43.86/jingwei/api/JingweiGateWay.do?act=getBinlogPosByTddlGroup&group=XX&user=XX&password=XX&host=1
			command = new GetBinlogPosByTddlGroupCommand(request, response, positionHelper);
		}
		if (act.equalsIgnoreCase(GetMysqlBinlogPosByTaskName.CMD_STR)) {
			// http://10.13.43.86/jingwei/api/JingweiGateWay.do?act=getMysqlBinlogPosByTaskName&taskName=XX&host=1
			command = new GetMysqlBinlogPosByTaskName(request, response);
			((GetMysqlBinlogPosByTaskName) command).setJwTaskAO(jwTaskAO);
			((GetMysqlBinlogPosByTaskName) command).setPositionHelper(positionHelper);
		}
		if (act.equalsIgnoreCase(UpdateMysqlBinlogPosCommand.CMD_STR)) {
			// http://10.13.43.86/jingwei/api/JingweiGateWay.do?act=updateMysqlBinlogPosCommand&taskName=XX&host=1
			command = new UpdateMysqlBinlogPosCommand(request, response);
			((UpdateMysqlBinlogPosCommand) command).setJwTaskAO(jwTaskAO);
			((UpdateMysqlBinlogPosCommand) command).setJwConfigManager(jwConfigManager);
		}
		if (act.equalsIgnoreCase(ToggleGroupBatchCommand.CMD_STR)) {
			// http://10.13.43.86/jingwei/api/JingweiGateWay.do?act=toggleGroupBatchModify&groupName=XX&host=1&supportBatchUpdate=true
			command = new ToggleGroupBatchCommand(request, response);
			((ToggleGroupBatchCommand) command).setJwGroupAO(jwGroupAO);
		}
		// 停止、启动一个组内的任务
		if (act.equalsIgnoreCase(ToggleTaskUnderGroupCommand.CMD_STR)) {
			command = new ToggleTaskUnderGroupCommand(request, response);
			((ToggleTaskUnderGroupCommand) command).setJwGroupAO(jwGroupAO);
		}
		// 批量修改位点
		// 停止、启动一个组内的任务
		if (act.equalsIgnoreCase(BatchUpdateMysqlBinlogPosCommand.CMD_STR)) {
			command = new BatchUpdateMysqlBinlogPosCommand(request, response);
			((BatchUpdateMysqlBinlogPosCommand) command).setJwTaskAO(jwTaskAO);
			((BatchUpdateMysqlBinlogPosCommand) command).setJwConfigManager(jwConfigManager);
		}

		// 还原tar包
		if (act.equalsIgnoreCase(RevertTarCommand.CMD_STR)) {
			command = new RevertTarCommand(request, response);
		}

		// 删除permissoin根据Id
		// http://10.13.43.86/jingwei/api/JingweiGateWay.do?act=deletePermissionById&id=XX
		if (act.equalsIgnoreCase(DeletePermissionCommand.CMD_STR)) {
			command = new DeletePermissionCommand(request, response);
			((DeletePermissionCommand) command).setJwPermissionDao(jwPermissionDao);
		}
		// 获取任务名
		// http://10.13.43.86/jingwei/api/JingweiGateWay.do?act=getTasksCommand&taskName=XX&host=1
		if (act.equalsIgnoreCase(GetTasksCommand.CMD_STR)) {
			command = new GetTasksCommand(request, response);
			((GetTasksCommand) command).setJwTaskAO(jwTaskAO);
		}

		// 获取group
		// http://10.13.43.86/jingwei/api/JingweiGateWay.do?act=getGroupsCommand&groupName=XX&host=1
		if (act.equalsIgnoreCase(GetGroupsCommand.CMD_STR)) {
			command = new GetGroupsCommand(request, response);
			((GetGroupsCommand) command).setJingweiGroupAO(jwGroupAO);
		}

		// 获取server
		// http://10.13.43.86/jingwei/api/JingweiGateWay.do?act=getServersCommand&serverName=XX&host=1
		if (act.equalsIgnoreCase(GetServersCommand.CMD_STR)) {
			command = new GetServersCommand(request, response);
			((GetServersCommand) command).setJingweiServerAO(jwServerAO);
		}

		// 添加（save）资源（任务、机器、group） 1:任务 2:机器 3:GROUP
		// http://10.13.43.86/jingwei/api/JingweiGateWay.do?act=saveResources&taskName=XX&host=1&resourceType=1
		if (act.equalsIgnoreCase(SaveResourcesCommand.CMD_STR)) {
			command = new SaveResourcesCommand(request, response);
			((SaveResourcesCommand) command).setJwResourceDao(jwResourceDao);
			((SaveResourcesCommand) command).setJwPermissionDao(jwPermissionDao);
		}

		//http://10.13.43.86/jingwei/api/JingweiGateWay.do?act=userRoleCommand&type=save&host=1&nickName=XXX&roleName=XXX
		//http://10.13.43.86/jingwei/api/JingweiGateWay.do?act=userRoleCommand&type=delete&host=1&id=XXX
		if (act.equalsIgnoreCase(JwUserRoleCommand.CMD_STR)) {
			command = new JwUserRoleCommand(request, response);
			((JwUserRoleCommand) command).setJwUserRoleDao(jwUserRoleDao);
			((JwUserRoleCommand) command).setJwUserRoleDao(jwUserRoleDao);
		}

		// 获取文件tar包在consoole上的创建时间
		//http://10.13.43.86/jingwei/api/JingweiGateWay.do?act=getConsoleTarTime&tarName=XXX&host=1
		if (act.equalsIgnoreCase(GetConsoleTarTimeCommand.CMD_STR)) {
			command = new GetConsoleTarTimeCommand(request, response);
		}

		// 如果失败则写会空字串
		if (command != null) {
			try {
				command.invoke();
			} catch (JSONException e) {
				JsonUtil.writeStr2Client(StringUtil.EMPTY_STRING, response);
			}
		}
	}
}
