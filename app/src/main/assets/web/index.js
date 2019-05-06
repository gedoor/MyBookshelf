
// 读写Hash值(val未赋值时为读取)
function hashParam(key, val) {
	let hashstr = decodeURIComponent(window.location.hash);
	let regKey = new RegExp(`${key}=(.*?&|.*$)`);
	let getVal = regKey.test(hashstr) ? hashstr.match(regKey)[1].replace('&', '') : null;
	if (val == undefined) return getVal;
	if (hashstr == '') window.location.hash = `#${key}=${val}`;
	window.location.hash = getVal ? hashstr.replace(getVal, val) : `${hashstr.replace(`&${key}=`)}&${key}=${val}`;
}
function addRule(rule) {
	return `<label for="${rule.bookSourceUrl}"><input type="radio" name="rule" id="${rule.bookSourceUrl}"><div>${rule.bookSourceName}<br>${rule.bookSourceUrl}</div></label>`;
}
function dQuery(selector) {
	return document.querySelector(selector)
}
function dQueryAll(selector) {
	return document.querySelectorAll(selector)
}
function showTab(tabName) {
	dQueryAll('.tabtitle>*').forEach(node => { node.className = node.className.replace(' this', ''); });
	dQueryAll('.tabbody>*').forEach(node => { node.className = node.className.replace(' this', ''); });
	dQuery(`.tabbody>.${dQuery(`.tabtitle>*[name=${tabName}]`).className}`).className += ' this';
	dQuery(`.tabtitle>*[name=${tabName}]`).className += ' this';
	hashParam('tab', tabName);
}
window.onload = () => {
	dQueryAll('.tabtitle>*').forEach(item => {
		item.addEventListener('click', () => {
			showTab(item.innerHTML);
		});
	});
	if (hashParam('tab')) showTab(hashParam('tab'));
}
// 定义规则对象
var RuleJSON = {
	"bookSourceName": "",
	"bookSourceGroup": "",
	"bookSourceUrl": "",
	"loginUrl": "",
	"ruleFindUrl": "",
	"ruleSearchUrl": "",
	"ruleSearchList": "",
	"ruleSearchName": "",
	"ruleSearchAuthor": "",
	"ruleSearchKind": "",
	"ruleSearchLastChapter": "",
	"ruleSearchCoverUrl": "",
	"ruleSearchNoteUrl": "",
	"ruleBookUrlPattern": "",
	"ruleBookName": "",
	"ruleBookAuthor": "",
	"ruleBookKind": "",
	"ruleBookLastChapter": "",
	"ruleIntroduce": "",
	"ruleCoverUrl": "",
	"ruleChapterUrl": "",
	"ruleChapterList": "",
	"ruleChapterUrlNext": "",
	"ruleChapterName": "",
	"ruleContentUrl": "",
	"ruleBookContent": "",
	"ruleContentUrlNext": "",
	"httpUserAgent": "",
	"serialNumber": 0,
	"weight": 0,
	"enable": true
};
// 获取数据
function HttpGet(url) {
	return fetch(hashParam('domain') ? hashParam('domain') + url : url)
		.then(res => res.json()).catch(err => console.error('Error:', err));
}
// 提交数据
function HttpPost(url, data) {
	return fetch(hashParam('domain') ? hashParam('domain') + url : url, {
		body: JSON.stringify(data),
		method: 'POST',
		mode: "cors",
		headers: new Headers({
			'Content-Type': 'application/json;charset=utf-8'
		})
	}).then(res => res.json()).catch(err => console.error('Error:', err));
}
// 将书源表单转化为书源对象
function rule2json() {
	Object.keys(RuleJSON).forEach((key) => { RuleJSON[key] = dQuery('#' + key).value; });
	RuleJSON.serialNumber = RuleJSON.serialNumber == '' ? 0 : parseInt(RuleJSON.serialNumber);
	RuleJSON.weight = RuleJSON.weight == '' ? 0 : parseInt(RuleJSON.weight);
	RuleJSON.enable = RuleJSON.enable == '' || RuleJSON.enable.toLocaleLowerCase().replace(/^\s*|\s*$/g, '') == 'true';
	return RuleJSON;
}
// 将书源对象填充到书源表单
function json2rule(RuleEditor) {
	Object.keys(RuleJSON).forEach((key) => { dQuery("#" + key).value = RuleEditor[key] });
}
// 缓存规则列表
var ruleSources = [];
if (localStorage.getItem('ruleSources')) {
	ruleSources = JSON.parse(localStorage.getItem('ruleSources'));
	ruleSources.forEach(item => {
		dQuery('#RuleList').innerHTML += addRule(item);
	});
}
// 记录操作过程
var course = { "old": [], "now": {}, "new": [] };
if (localStorage.getItem('course')) {
	course = JSON.parse(localStorage.getItem('course'));
	json2rule(course.now);
}
else {
	course.now = rule2json();
	window.localStorage.setItem('course', JSON.stringify(course));
}
function todo() {
	course.old.push(Object.assign({}, course.now));
	course.now = rule2json();
	course.new = [];
	if (course.old.length > 50) course.old.shift(); // 限制历史记录堆栈大小
	localStorage.setItem('course', JSON.stringify(course));
}
function undo() {
	course = JSON.parse(localStorage.getItem('course'));
	if (course.old.length > 0) {
		course.new.push(course.now);
		course.now = course.old.pop();
		localStorage.setItem('course', JSON.stringify(course));
		json2rule(course.now);
	}
}
function redo() {
	course = JSON.parse(localStorage.getItem('course'));
	if (course.new.length > 0) {
		course.old.push(course.now);
		course.now = course.new.pop();
		localStorage.setItem('course', JSON.stringify(course));
		json2rule(course.now);
	}
}
function setRule(editRule) {
	let checkRule = ruleSources.find(x => x.bookSourceUrl == editRule.bookSourceUrl);
	if (dQuery(`input[id="${editRule.bookSourceUrl}"]`)) {
		Object.keys(checkRule).forEach(key => { checkRule[key] = editRule[key]; });
		dQuery(`input[id="${editRule.bookSourceUrl}"]+*`).innerHTML = `${editRule.bookSourceName}<br>${editRule.bookSourceUrl}`;
	} else {
		ruleSources.push(editRule);
		dQuery('#RuleList').innerHTML += addRule(editRule);
	}
}
dQueryAll('input').forEach((item) => { item.addEventListener('change', () => { todo() }) });
dQueryAll('textarea').forEach((item) => { item.addEventListener('change', () => { todo() }) });
// 处理按钮点击事件
dQuery('.menu').addEventListener('click', e => {
	let thisNode = e.target;
	if (thisNode.nodeName == 'text') thisNode = thisNode.nextSibling;
	if (thisNode && thisNode.nodeName == 'rect') { } else return;
	if (thisNode.getAttribute('class') == 'busy') return;
	thisNode.setAttribute('class', 'busy');
	switch (thisNode.id) {
		case 'push':
			dQueryAll('#RuleList>label>div').forEach(item => { item.className = ''; });
			(async () => {
				await HttpPost(`/saveSources`, ruleSources).then(json => {
					if (json.isSuccess) {
						let okData = json.data;
						if (Array.isArray(okData)) {
							let failMsg = ``;
							if (ruleSources.length > okData.length) {
								ruleSources.forEach(item => {
									if (okData.find(x => x.bookSourceUrl == item.bookSourceUrl)) { }
									else { dQuery(`#RuleList #${item.bookSourceUrl}+*`).className += 'isError'; }
								});
								failMsg = '\n推送失败的书源将用红色字体标注!';
							}
							alert(`批量推送书源到「阅读APP」\n共计: ${ruleSources.length} 条\n成功: ${okData.length} 条\n失败: ${ruleSources.length - okData.length} 条${failMsg}`);
						}
						else {
							alert(`批量推送书源到「阅读APP」成功!\n共计: ${ruleSources.length} 条`);
						}
					}
					else {
						alert(`批量推送书源失败!\nErrorMsg: ${json.errorMsg}`);
					}
				}).catch(err => { alert(`批量推送书源失败,无法连接到「阅读APP」!\n${err}`); });
				thisNode.setAttribute('class', '');
			})();
			return;
		case 'pull':
			showTab('书源列表');
			(async () => {
				await HttpGet(`/getSources`).then(json => {
					if (json.isSuccess) {
						dQuery('#RuleList').innerHTML = ''
						localStorage.setItem('ruleSources', JSON.stringify(ruleSources = json.data));
						ruleSources.forEach(item => {
							dQuery('#RuleList').innerHTML += addRule(item);
						});
						alert(`成功拉取 ${ruleSources.length} 条书源`);
					}
					else {
						alert(`批量拉取书源失败!\nErrorMsg: ${json.errorMsg}`);
					}
				}).catch(err => { alert(`批量拉取书源失败,无法连接到「阅读APP」!\n${err}`); });
				thisNode.setAttribute('class', '');
			})();
			return;
		case 'editor':
			if (dQuery('#RuleJsonString').value == '') break;
			try {
				json2rule(JSON.parse(dQuery('#RuleJsonString').value));
				todo();
			} catch (error) {
				console.log(error);
				alert(error);
			}
			break;
		case 'conver':
			showTab('编辑书源');
			dQuery('#RuleJsonString').value = JSON.stringify(rule2json(), null, 4);
			break;
		case 'initial':
			dQueryAll('.rules textarea').forEach(item => { item.value = '' });
			todo();
			break;
		case 'undo':
			undo()
			break;
		case 'redo':
			redo()
			break;
		case 'debug':
			showTab('调试书源');
			let wsHost = (hashParam('domain') || location.host).replace(/.*\//, '').split(':');
			function DebugPrint(msg) { dQuery('#DebugConsole').value += `\n${msg}` }
			(async () => {
				let saveRule = [rule2json()];
				let sResult = await HttpPost(`/saveSources`, saveRule);
				if (sResult.isSuccess) {
					let sKey = DebugKey.value ? DebugKey.value : '我的';
					dQuery('#DebugConsole').value = `书源《${saveRule[0].bookSourceName}》保存成功！使用搜索关键字“${sKey}”开始调试...`;
					let ws = new WebSocket(`ws://${wsHost[0]}:${parseInt(wsHost[1]) + 1}/sourceDebug`);
					ws.onopen = () => {
						ws.send(`{"tag":"${saveRule[0].bookSourceUrl}", "key":"${sKey}"}`);
					};
					ws.onmessage = (msg) => {
						if (msg.data == 'finish') {
							DebugPrint(`成功完成调试任务!`);
						} else {
							DebugPrint(msg.data);
						}
					};
					ws.onerror = (err) => { throw `${err.data}`; }
					ws.onclose = () => { thisNode.setAttribute('class', ''); }
				} else throw `${sResult.errorMsg}`;
			})().catch(err => {
				DebugPrint(`调试过程意外中止，以下是详细错误信息:\n${err}`);
				thisNode.setAttribute('class', '');
			});
			return;
		case 'accept':
			(async () => {
				let saveRule = [rule2json()];
				await HttpPost(`/saveSources`, saveRule).then(json => {
					alert(json.isSuccess ? `书源《${saveRule[0].bookSourceName}》已成功保存到「阅读APP」` : `书源《${saveRule[0].bookSourceName}》保存失败!\nErrorMsg: ${json.errorMsg}`);
					setRule(saveRule[0]);
				}).catch(err => { alert(`保存书源失败,无法连接到「阅读APP」!\n${err}`); });
				thisNode.setAttribute('class', '');
			})();
			return;
		default:
	}
	setTimeout(() => { thisNode.setAttribute('class', ''); }, 500);
});

// 列表规则更改事件
dQuery('#RuleList').addEventListener('click', e => {
	let editRule = null;
	if (e.target && e.target.getAttribute('name') == 'rule') {
		editRule = rule2json();
		json2rule(ruleSources.find(x => x.bookSourceUrl == e.target.id));
	} else return;
	if (editRule.bookSourceUrl == '') return;
	if (editRule.bookSourceName == '') editRule.bookSourceName = editRule.bookSourceUrl.replace(/.*?\/\/|\/.*/g, '');
	setRule(editRule);
	localStorage.setItem('ruleSources', JSON.stringify(ruleSources));
});