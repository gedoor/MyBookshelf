unit uFrmMain;

interface

uses
  iocp.Http.Client, iocp.Utils.Str,
  YxdJson, YxdStr, YxdHash, YxdWorker, ShellAPI, Math, StrUtils,
  uBookSourceBean,
  Windows, Messages, SysUtils, Variants, Classes, Graphics, Controls, Forms,
  Dialogs, StdCtrls, ExtCtrls, Menus, SynEdit, SynMemo, ComCtrls, SyncObjs,
  SynEditHighlighter, SynHighlighterJSON, Vcl.Buttons;

type
  PProcessState = ^TProcessState;
  TProcessState = record
    STime: Int64;
    NeedFree: Boolean;
    Min: Integer;
    Max: Integer;
    Value: Integer;
  end;

type
  TForm1 = class(TForm)
    Panel1: TPanel;
    SrcList: TListBox;
    Splitter1: TSplitter;
    Panel2: TPanel;
    PopupMenu1: TPopupMenu;
    C1: TMenuItem;
    Panel3: TPanel;
    EditData: TSynMemo;
    Button1: TButton;
    Panel4: TPanel;
    lbCount: TLabel;
    bookGroupList: TComboBox;
    StatusBar1: TStatusBar;
    ProgressBar1: TProgressBar;
    SynJSONSyn1: TSynJSONSyn;
    PopupMenu2: TPopupMenu;
    S1: TMenuItem;
    N1: TMenuItem;
    C2: TMenuItem;
    X1: TMenuItem;
    P1: TMenuItem;
    A1: TMenuItem;
    N2: TMenuItem;
    N3: TMenuItem;
    R1: TMenuItem;
    Z1: TMenuItem;
    N4: TMenuItem;
    W1: TMenuItem;
    Label1: TLabel;
    Edit1: TEdit;
    CheckBox1: TCheckBox;
    CheckBox2: TCheckBox;
    D1: TMenuItem;
    N6: TMenuItem;
    C3: TMenuItem;
    N5: TMenuItem;
    S2: TMenuItem;
    G1: TMenuItem;
    N7: TMenuItem;
    E1: TMenuItem;
    Timer1: TTimer;
    CheckBox3: TCheckBox;
    MainMenu1: TMainMenu;
    F1: TMenuItem;
    H1: TMenuItem;
    E2: TMenuItem;
    I1: TMenuItem;
    SaveDialog1: TSaveDialog;
    W2: TMenuItem;
    N8: TMenuItem;
    R2: TMenuItem;
    N9: TMenuItem;
    H2: TMenuItem;
    E3: TMenuItem;
    H3: TMenuItem;
    N10: TMenuItem;
    N11: TMenuItem;
    T1: TMenuItem;
    Panel5: TPanel;
    Splitter2: TSplitter;
    Label2: TLabel;
    edtLog: TSynMemo;
    SpeedButton1: TSpeedButton;
    StaticText1: TStaticText;
    O1: TMenuItem;
    N12: TMenuItem;
    OpenDialog1: TOpenDialog;
    A2: TMenuItem;
    procedure FormCreate(Sender: TObject);
    procedure C1Click(Sender: TObject);
    procedure FormShow(Sender: TObject);
    procedure SrcListKeyDown(Sender: TObject; var Key: Word;
      Shift: TShiftState);
    procedure FormDestroy(Sender: TObject);
    procedure SrcListData(Control: TWinControl; Index: Integer;
      var Data: string);
    procedure PopupMenu2Popup(Sender: TObject);
    procedure R1Click(Sender: TObject);
    procedure Z1Click(Sender: TObject);
    procedure C2Click(Sender: TObject);
    procedure X1Click(Sender: TObject);
    procedure P1Click(Sender: TObject);
    procedure A1Click(Sender: TObject);
    procedure S1Click(Sender: TObject);
    procedure SrcListClick(Sender: TObject);
    procedure EditDataChange(Sender: TObject);
    procedure W1Click(Sender: TObject);
    procedure D1Click(Sender: TObject);
    procedure C3Click(Sender: TObject);
    procedure Button1Click(Sender: TObject);
    procedure Timer1Timer(Sender: TObject);
    procedure bookGroupListChange(Sender: TObject);
    procedure N7Click(Sender: TObject);
    procedure E1Click(Sender: TObject);
    procedure SrcListDblClick(Sender: TObject);
    procedure I1Click(Sender: TObject);
    procedure E2Click(Sender: TObject);
    procedure W2Click(Sender: TObject);
    procedure R2Click(Sender: TObject);
    procedure S2Click(Sender: TObject);
    procedure G1Click(Sender: TObject);
    procedure H2Click(Sender: TObject);
    procedure T1Click(Sender: TObject);
    procedure SpeedButton1Click(Sender: TObject);
    procedure O1Click(Sender: TObject);
    procedure A2Click(Sender: TObject);
  private
    { Private declarations }
    OldListWndProc, OldTextWndProc: TWndMethod;
    FBookSrcData: JSONArray;
    FBookGroups: TStringHash;
    FIsChange, FChanging: Boolean;
    FCurIndex: Integer;

    FWaitStop: Integer;
    FTaskRef: Integer;
    FTaskStartTime: Int64;
    FCheckLastTime: Int64;

    FFilterList: TList;
    FCurCheckIndex: Integer;
    FCheckCount: Integer;

    FWaitCheckBookSourceSingId: Integer;

    FLocker: TCriticalSection;
    FStateMsg: string;

  protected
    procedure SrcListWndProc(var Message: TMessage);
    procedure SrcListTextWndProc(var Message: TMessage);
    procedure AddSrcFiles(ADrop: Integer);
    procedure WMDropFiles(var Msg: TWMDropFiles); message WM_DROPFILES;
  public
    { Public declarations }
    function CheckItem(Item: TBookSourceItem): Boolean;
    function CheckSaveState(): Boolean;


    function CheckBookSourceItem(Item: TBookSourceItem; RaiseErr: Boolean = False; OutLog: TStrings = nil): Boolean; overload;
    function CheckBookSourceItem(Item: TBookSourceItem; Http: THttpClient; Header: THttpHeaders; RaiseErr: Boolean = False; OutLog: TStrings = nil): Boolean; overload;

    procedure AddSrcFile(const FileName: string);
    procedure UpdateBookGroup(Item: TBookSourceItem);
    
    procedure RemoveRepeat(AJob: PJob);  
    procedure WaitCheckBookSource(AJob: PJob);
    procedure DoCheckBookSourceItem(AJob: PJob);
    
    procedure TaskFinish(AJob: PJob);
    procedure DoNotifyDataChange(AJob: PJob);
    procedure DoUpdateProcess(AJob: PJob);

    procedure Log(const Msg: string);
    procedure LogD(const Msg: string);
    procedure DispLog();
    procedure NotifyListChange(Flag: Integer = 0);

    procedure RemoveSelected();
    procedure EditSource(Item: TBookSourceItem);
    
  end;

var
  Form1: TForm1;

implementation

{$R *.dfm}

uses
  uFrmWait, uFrmEditSource, uFrmReplaceGroup;

procedure CutOrCopyFiles(FileList: AnsiString; bCopy: Boolean);
type
  PDropFiles = ^TDropFiles;
 
  TDropFiles = record
    pfiles: DWORD;
    pt: TPoint;
    fNC: BOOL;
    fwide: BOOL;
  end;
const
  DROPEFFECT_COPY = 1;
  DROPEFFECT_MOVE = 2;
var
  hGblFileList: hGlobal;
  pFileListDate: Pbyte;
  HandleDropEffect: UINT;
  hGblDropEffect: hGlobal;
  pdwDropEffect: PDWORD;
  iLen: Integer;
begin
  iLen := Length(FileList) + 2;
  FileList := FileList + #0#0;
  hGblFileList := GlobalAlloc(GMEM_ZEROINIT or GMEM_MOVEABLE or GMEM_SHARE,
    SizeOf(TDropFiles) + iLen);
  pFileListDate := GlobalLock(hGblFileList);
  PDropFiles(pFileListDate)^.pfiles := SizeOf(TDropFiles);
  PDropFiles(pFileListDate)^.pt.Y := 0;
  PDropFiles(pFileListDate)^.pt.X := 0;
  PDropFiles(pFileListDate)^.fNC := False;
  PDropFiles(pFileListDate)^.fwide := False;
  Inc(pFileListDate, SizeOf(TDropFiles));
  CopyMemory(pFileListDate, @FileList[1], iLen);
  GlobalUnlock(hGblFileList);
  HandleDropEffect := RegisterClipboardFormat('Preferred DropEffect ');
  hGblDropEffect := GlobalAlloc(GMEM_ZEROINIT or GMEM_MOVEABLE or GMEM_SHARE,
    SizeOf(DWORD));
  pdwDropEffect := GlobalLock(hGblDropEffect);
  if (bCopy) then pdwDropEffect^ := DROPEFFECT_COPY
  else pdwDropEffect^ := DROPEFFECT_MOVE;
  GlobalUnlock(hGblDropEffect);
  if OpenClipboard(0) then begin
    EmptyClipboard();
    SetClipboardData(HandleDropEffect, hGblDropEffect);
    SetClipboardData(CF_HDROP, hGblFileList);
    CloseClipboard();
  end;
end;

// 复制文件，多个文件以 #0 分隔
procedure CopyFileClipbrd(const FName: string);
begin
  CutOrCopyFiles(AnsiString(FName), True);
end;

procedure TForm1.AddSrcFiles(ADrop: Integer);
var
  i: Integer;
  p: array[0..1023] of Char;
begin
  for i := 0 to DragQueryFile(ADrop, $FFFFFFFF, nil, 0) - 1 do begin
    DragQueryFile(ADrop, i, p, 1024);
    AddSrcFile(StrPas(p));
  end;
end;

procedure TForm1.bookGroupListChange(Sender: TObject);
begin
  if FChanging then
    Exit; 
  FChanging := True;
  try     
    EditData.Text := '';
    FIsChange := False;
    FCurIndex := -1;
    NotifyListChange(1);
  finally
    FChanging := False;
  end;
end;

procedure TForm1.Button1Click(Sender: TObject);
begin
  if Button1.Tag = 0 then begin
    Button1.Tag := 1;
    Button1.Caption := '停止(&S)';
    
    FTaskRef := 0;
    FWaitStop := 0;
    FCheckLastTime := GetTimestamp;
    FTaskStartTime := GetTimestamp;

    Workers.MaxWorkers := Max(1, StrToIntDef(Edit1.Text, 8));

    edtLog.Lines.Clear;
    Log('正在初始化任务...');

    Timer1.Enabled := True;
    ProgressBar1.Min := 0;
    ProgressBar1.Max := 100;
    ProgressBar1.Position := 0;      

    if CheckBox1.Checked then begin
      Inc(FTaskRef);
      Log('正在去重复...');
      Workers.Post(RemoveRePeat, Pointer(Integer(CheckBox3.Checked)));
    end;
    if CheckBox2.Checked then begin
      Inc(FTaskRef);
      if not CheckBox1.Checked then
        Workers.SendSignal(FWaitCheckBookSourceSingId);
    end;

    ShowWaitDlg();
  end else begin
    AtomicIncrement(FTaskRef);
    if AtomicDecrement(FTaskRef) <= 0 then begin
      Button1.Tag := 0;
      Button1.Caption := '开始处理(&B)';
      Timer1.Enabled := False;
      FTaskStartTime := 0;
      ProgressBar1.Visible := False;
      HideWaitDlg;
      Log('任务结束');
    end else begin        
      Button1.Tag := 2;
      Button1.Caption := '正在停止...';      
      AtomicIncrement(FWaitStop);
    end;
  end;
  //Application.ProcessMessages;
end;

procedure TForm1.A1Click(Sender: TObject);
begin
  EditData.SelectAll;
end;

procedure TForm1.A2Click(Sender: TObject);
begin
  OpenDialog1.Title := '添加书源文件';
  if OpenDialog1.Execute(Handle) then
    AddSrcFile(OpenDialog1.FileName);
end;

procedure TForm1.AddSrcFile(const FileName: string);
var
  Data: JSONArray;
  Item: TBookSourceItem;
  I: Integer;
begin
  Data := JSONArray.Create;
  try    
    Data.LoadFromFile(FileName);
    I := Data.Count;
    while I > 0 do begin
      try
        Item := TBookSourceItem(Data.O[0]);
        if Assigned(Item) and (Item.bookSourceUrl <> '') then begin
          UpdateBookGroup(Item);
          FBookSrcData.Add(Data.O[0]);
        end;
      except
      end;
      Dec(I);
    end;
  finally
    Data.Free;
    NotifyListChange;
  end;
end;

procedure TForm1.C1Click(Sender: TObject);
begin
  FBookSrcData.Clear;
  NotifyListChange;
end;

procedure TForm1.C2Click(Sender: TObject);
begin
  EditData.CopyToClipboard;
end;

procedure TForm1.C3Click(Sender: TObject);
var
  S: string;
  Item, NewItem: TBookSourceItem;
begin
  if SrcList.ItemIndex < 0 then Exit;
  if not CheckSaveState then Exit;
  Item := TBookSourceItem(FFilterList[SrcList.ItemIndex]);
  S := InputBox('书源名称', '请输入书源名称', Item.bookSourceName);

  NewItem := TBookSourceItem(FBookSrcData.AddChildObject);
  NewItem.Parse(Item.ToString);
  NewItem.bookSourceName := S;

  NotifyListChange;
  if FFilterList.Count > 0 then    
    SrcList.ItemIndex := FFilterList.Count - 1;
end;

function TForm1.CheckBookSourceItem(Item: TBookSourceItem; RaiseErr: Boolean; OutLog: TStrings): Boolean;
var
  Http: THttpClient;
  Header: THttpHeaders;
begin
  Result := False;
  try
    if not Assigned(Item) then Exit; 
    Http := THttpClient.Create(nil);
    if Assigned(OutLog) then begin    
      Http.ConnectionTimeOut := 6000;
      Http.RecvTimeOut := 30000;
    end else begin
      Http.ConnectionTimeOut := 30000;
      Http.RecvTimeOut := 30000;
    end;
    Header := THttpHeaders.Create;

    Result := CheckBookSourceItem(Item, Http, Header, RaiseErr, OutLog);
    
  finally
    FreeAndNil(Http);
    FreeAndNil(Header);
  end;
end;

function TForm1.CheckBookSourceItem(Item: TBookSourceItem; Http: THttpClient;
  Header: THttpHeaders; RaiseErr: Boolean; OutLog: TStrings): Boolean;

  function CheckURL(const URL, Title: string; RaiseErr: Boolean = False; Try404: Boolean = False): Boolean;
  var
    Resp: THttpResult;
    Msg: string;
  begin
    Result := (URL <> '') and (URL <> '-') and (Pos('http', LowerCase(URL)) = 1); 
    if Result then begin
      try
        Resp := Http.Get(UrlEncodeEx(URL), nil, Header);
        if (Resp.StatusCode = 200) or (Try404 and (Resp.StatusCode = 404)) then begin       
          Result := True;
          if Assigned(OutLog) then OutLog.Add(Title + '连接成功.');
        end else begin
          Result := False;
          Msg := Format('%s测试失败(StatusCode: %d, %s).', [Title, Resp.StatusCode, URL]);
          if Assigned(OutLog) then OutLog.Add(Msg);
          if RaiseErr then
            raise Exception.Create(Msg);
        end;
      except
        Result := False;
        Msg := Format('%s测试出错(%s).', [Title, Exception(ExceptObject).Message]);
        if Assigned(OutLog) then OutLog.Add(Msg);
        if RaiseErr then
          raise Exception.Create(Msg);
      end;
    end else
      OutLog.Add('无效的' + Title + '.');
  end;

  // 检测发现列表
  function CheckFindURL(const Text, Title: string; RaiseErr: Boolean): Boolean;
  var 
    List: TStrings;
    I, J, L: Integer;
    Msg, Item, SubTitle, AURL: string;
  begin
    if Text = '' then begin
      Result := True;
      Exit;
    end;
    try
      J := 1;
      while (J > 0) and (J <= Length(Text)) do begin        
        I := PosEx('&&', Text, J);
        L := 2;
        if I <= 0 then begin
          I := PosEx(#$A, Text, J);
          L := 1;
        end;
        if I > 0 then begin
          Item := MidStr(Text, J, I - J);
          J := I + L;
        end else begin
          Item := Trim(RightStr(Text, Length(Text) - J + 1));
          J := Length(Text) + 1;
        end;

        if (Item = #$A) or (Item = #13) then
          Continue;

        Item := StringReplace(Item, #13, '', [rfReplaceAll]);
        Item := StringReplace(Item, #10, '', [rfReplaceAll]);
        Item := StringReplace(Item, '\n', '', [rfReplaceAll, rfIgnoreCase]);
        Item := Trim(Item);  
              
        I := Pos('::', Item);
        if (Item = '') or (I < 1) then begin
          if Assigned(OutLog) then
            OutLog.Add('发现列表格式错误');
          Continue;
        end else begin
          SubTitle := Trim(LeftStr(Item, I - 1));
          AURL := Trim(RightStr(Item, Length(Item) - I - 1));
          CheckURL(AURL, '发现列表项【' + SubTitle + '】');   
        end;
      end;
    except
      Result := False;
      Msg := Format('%s测试出错(%s).', [Title, Exception(ExceptObject).Message]);
      if Assigned(OutLog) then OutLog.Add(Msg);
      if RaiseErr then
        raise Exception.Create(Msg);
    end;
  end;
  
var
  Resp: THttpResult;
  URL: string;
  T: Int64;
begin
  Result := False;
  if not Assigned(Item) then Exit; 
  if Item.bookSourceUrl <> '' then begin
    T := GetTimestamp;
    Header.Clear;
    if Item.httpUserAgent <> '' then
      Header.Add('User-Agent', Item.httpUserAgent);

    // 检测书源URL
    Result := CheckURL(Trim(Item.bookSourceUrl), '书源URL', RaiseErr, True);

    if Result and Assigned(OutLog) then begin
      // 检测搜索URL
      CheckURL(Trim(Item.ruleSearchUrl), '搜索地址');
      // 检测发现列表
      CheckFindURL(Trim(Item.ruleFindUrl), '发现', RaiseErr);
    end;

    if Assigned(OutLog) then
      OutLog.Add(Format('用时 %d ms.', [GetTimestamp - T]));
  end else begin
    if Assigned(OutLog) then
      OutLog.Add('书源URL未设置.');
    raise Exception.Create('书源URL无效');
  end;
end;

function TForm1.CheckItem(Item: TBookSourceItem): Boolean;
begin
  Result := Assigned(Item) and (Item.bookSourceUrl <> '');
end;

function TForm1.CheckSaveState: Boolean;
var
  LR: Integer;
begin
  if FIsChange and (FCurIndex >= 0) and (FCurIndex < SrcList.Count) then begin
    LR := MessageBox(Handle, '书源内容已经修改，是否保存？', '提示', 64 + MB_YESNOCANCEL);
    if LR = IDCANCEL then begin
      Result := False;
      Exit;
    end;
    if LR = IDYES then
      S1Click(S1);
  end;
  Result := True;
end;

procedure TForm1.D1Click(Sender: TObject);
begin
  RemoveSelected;
end;

procedure TForm1.DispLog;
begin
  if FTaskStartTime > 0 then begin 
    if ProgressBar1.Visible then
      StatusBar1.Panels[1].Text := Format('%s (%d/%d, %d%%) (用时: %dms)', 
        [FStateMsg, ProgressBar1.Position, ProgressBar1.Max, Round(ProgressBar1.Position / ProgressBar1.Max * 100),
         GetTimestamp - FTaskStartTime])
    else  
      StatusBar1.Panels[1].Text := Format('%s (用时: %dms)', [FStateMsg, GetTimestamp - FTaskStartTime])
  end else
    StatusBar1.Panels[1].Text := FStateMsg;
end;

procedure TForm1.DoCheckBookSourceItem(AJob: PJob);
var
  Item: TBookSourceItem;
  State: PProcessState;
  V: Integer;
  IsOK: Boolean;
  Http: THttpClient;
  Header: THttpHeaders;
begin
  V := 0;
  try
    Http := THttpClient.Create(nil);
    Http.ConnectionTimeOut := 30000;
    Http.RecvTimeOut := 30000;
    Header := THttpHeaders.Create;
    
    while (not AJob.IsTerminated) and (FWaitStop = 0) do begin
      V := AtomicIncrement(FCurCheckIndex) - 1;
      
      FLocker.Enter;
      if (GetTimestamp - FCheckLastTime) > 100 then begin
        FCheckLastTime := GetTimestamp;
        New(State);
        State.Min := 0;
        State.Max := FCheckCount;
        State.Value := V;
        Workers.Post(DoUpdateProcess, State, True);
        Sleep(10);
      end;
      FLocker.Leave;
      
      if V < FCheckCount then begin
        Item := TBookSourceItem(FBookSrcData.O[V]);
        if not Assigned(Item) then Exit;

        try
          IsOK := CheckBookSourceItem(Item, Http, Header);
        except
          IsOK := False;
        end;
        
        if IsOK then
          Item.RemoveGroup('失效')
        else
          Item.AddGroup('失效');   
      end else
        Break;
    end;
  finally

    if (V >= FCheckCount) or (FWaitStop > 0) then begin
      Sleep(100);
      Workers.Post(TaskFinish, nil, True);
    end;

    FreeAndNil(Http);
    FreeAndNil(Header);
  end;
end;

procedure TForm1.DoNotifyDataChange(AJob: PJob);
begin
  NotifyListChange;
end;

procedure TForm1.DoUpdateProcess(AJob: PJob);
var
  V: PProcessState;
begin
  if not Assigned(Self) then Exit;  
  V := AJob.Data;
  if V = nil then
    ProgressBar1.Visible := False
  else begin
    ProgressBar1.Min := V.Min;
    ProgressBar1.Max := V.Max;
    ProgressBar1.Position := V.Value;
    ProgressBar1.Visible := Button1.Tag <> 0;
    if V.NeedFree then
      Dispose(V);
  end;
end;

procedure TForm1.E1Click(Sender: TObject);
begin
  if SrcList.ItemIndex < 0 then Exit;
  EditSource(TBookSourceItem(FFilterList[SrcList.ItemIndex]));
end;

procedure TForm1.E2Click(Sender: TObject);
var
  FName: JSONString;
begin
  if SaveDialog1.Execute(Handle) then begin
    FName := SaveDialog1.FileName;
    if ExtractFileExt(FName) = '' then
      FName := FName + '.json';
    FBookSrcData.SaveToFile(FName, 4, YxdStr.TTextEncoding.teUTF8, False);
  end;
end;

procedure TForm1.EditDataChange(Sender: TObject);
begin
  FIsChange := True;
end;

procedure TForm1.EditSource(Item: TBookSourceItem);
begin
  ShowEditSource(Item,
    procedure (Item: TBookSourceItem) 
    begin
      if FBookSrcData.IndexOfObject(Item) < 0 then
        FBookSrcData.Add(JSONObject(Item));  
      NotifyListChange;
      if (FCurIndex >= 0) and (FCurIndex < FFilterList.Count) then begin
        if TObject(FFilterList[FCurIndex]) = Item then begin        
          EditData.Text := TBookSourceItem(FFilterList[FCurIndex]).ToString(4);
          FIsChange := False;
        end;
      end;  
    end
  );
end;

procedure TForm1.FormCreate(Sender: TObject);
begin
  JsonNameAfterSpace := True;
  JsonCaseSensitive := False;
  FBookSrcData := JSONArray.Create;
  FBookGroups := TStringHash.Create(997);
  FFilterList := TList.Create;
  FLocker := TCriticalSection.Create;

  FWaitCheckBookSourceSingId := Workers.RegisterSignal('WaitCheckBookSource');
  Workers.PostWait(WaitCheckBookSource, FWaitCheckBookSourceSingId);
end;

procedure TForm1.FormDestroy(Sender: TObject);
begin
  FreeAndNil(FBookSrcData);
  FreeAndNil(FBookGroups);
  FreeAndNil(FFilterList);
  FreeAndNil(FLocker);
end;

procedure TForm1.FormShow(Sender: TObject);
begin
  DragAcceptFiles(SrcList.Handle, True);
  DragAcceptFiles(StaticText1.Handle, True);

  OldListWndProc := SrcList.WindowProc;
  OldTextWndProc := StaticText1.WindowProc;
  SrcList.WindowProc := SrcListWndProc;
  StaticText1.WindowProc := SrcListTextWndProc;

  NotifyListChange;
end;

procedure TForm1.G1Click(Sender: TObject);
begin
  FBookSrcData.Sort(
    function (A, B: Pointer): Integer
    var
      Item1: PJSONValue absolute A;
      Item2: PJSONValue absolute B;
      S1, S2: string;
    begin
      if (Item1.FType = Item2.FType) and (Item1.FType = jdtObject) and 
        (Item1.AsJsonObject <> nil) and (Item2.AsJsonObject <> nil) 
      then begin
        S1 := TBookSourceItem(Item1.AsJsonObject).bookSourceGroup;
        S2 := TBookSourceItem(Item2.AsJsonObject).bookSourceGroup;
        Result := CompareStr(S1, S2);
      end else
        Result := 0;
    end
  );
  NotifyListChange(1);
end;

procedure TForm1.H2Click(Sender: TObject);
var
  FindStr, NewStr: string;
  I, Flag: Integer;
  Item: TBookSourceItem;
begin
  if ShowReplaceGroup(Self, FindStr, NewStr, Flag) then begin
    if (FindStr <> '') and (Flag = 0) then    
      Exit;
    for I := 0 to FBookSrcData.Count - 1 do begin
      Item := TBookSourceItem(FBookSrcData.O[I]);
      if not Assigned(Item) then Continue;
      if Flag = 0 then begin              
        Item.bookSourceGroup := StringReplace(Trim(Item.bookSourceGroup), FindStr, NewStr, [rfReplaceAll, rfIgnoreCase]);
      end else begin
        if NewStr = '' then
          Item.RemoveGroup(FindStr)
        else
          Item.ReplaceGroup(FindStr, NewStr);
      end;      
    end;
    NotifyListChange();
  end;   
end;

procedure TForm1.I1Click(Sender: TObject);
var
  Msg: string;
begin
  Msg := Application.Title + sLineBreak + 'YangYxd 版权所有 2019';
  MessageBox(Handle, PChar(Msg), '关于我', 64);
end;

procedure TForm1.Log(const Msg: string);
begin
  LogD(Msg);
  FStateMsg := Msg;
  DispLog();
end;

procedure TForm1.LogD(const Msg: string);
begin
  edtLog.Lines.Add(Format('[%s] %s', [FormatDateTime('hh:mm:ss.zzz', Now), Msg]));
end;

procedure TForm1.N7Click(Sender: TObject);
var
  Item: TBookSourceItem;
begin
  Item := TBookSourceItem(JSONObject.Create);
  EditSource(Item);
end;

procedure TForm1.NotifyListChange(Flag: Integer);
var
  I, J: Integer;
  Key: string;
  Item: TBookSourceItem;
begin
  J := FCurIndex;
  
  if Flag = 0 then begin 
    for I := 0 to FBookSrcData.Count - 1 do
      UpdateBookGroup(TBookSourceItem(FBookSrcData.O[I])); 
    bookGroupList.Items.Clear;
    FBookGroups.GetKeyList(bookGroupList.Items);
  end;   

  FFilterList.Clear;
  Key := LowerCase(bookGroupList.Text);
  if Key <> '' then begin  
    for I := 0 to FBookSrcData.Count - 1 do begin
      Item := TBookSourceItem(FBookSrcData.O[I]);
      if (Pos(Key, Item.bookSourceGroup) > 0) or (Pos(Key, Item.bookSourceName) > 0) then
        FFilterList.Add(Item);      
    end;
  end else begin
    for I := 0 to FBookSrcData.Count - 1 do 
      FFilterList.Add(FBookSrcData.O[I]);  
  end;
  
  SrcList.Count := FFilterList.Count;
  StaticText1.Visible := SrcList.Count = 0;
  if (J < SrcList.Count) and (J >= 0) then begin
    SrcList.ClearSelection;
    SrcList.ItemIndex := J;
    SrcList.Selected[J] := True;
  end;
  
  SrcList.ShowHint := SrcList.Count = 0;
  StatusBar1.Panels[0].Text := Format('书源总数：%d个, 当前: %d个', [FBookSrcData.Count, FFilterList.Count]);
end;

procedure TForm1.O1Click(Sender: TObject);
begin
  OpenDialog1.Title := '打开书源文件';
  if OpenDialog1.Execute(Handle) then begin
    FBookSrcData.Clear;
    AddSrcFile(OpenDialog1.FileName);
  end;
end;

procedure TForm1.P1Click(Sender: TObject);
begin
  EditData.PasteFromClipboard;
end;

procedure TForm1.PopupMenu2Popup(Sender: TObject);
begin
  S1.Enabled := SrcList.ItemIndex >= 0;
  P1.Enabled := EditData.CanPaste;
  X1.Enabled := EditData.SelLength > 0;
  C2.Enabled := X1.Enabled;
  R1.Enabled := EditData.CanUndo;
  Z1.Enabled := EditData.CanRedo;
  W1.Checked := EditData.WordWrap;
end;

procedure TForm1.R1Click(Sender: TObject);
begin
  EditData.Undo;
end;

procedure TForm1.R2Click(Sender: TObject);
begin
  ShellExecute(0, 'OPEN', PChar('https://github.com/yangyxd/MyBookshelf'), nil, nil, SW_SHOWMAXIMIZED)
end;

procedure TForm1.RemoveRepeat(AJob: PJob);
var
  CheckName: Boolean;

  function Equals(A, B: TBookSourceItem): Boolean;
  begin
    Result := 
      (LowerCase(A.bookSourceUrl) = LowerCase(B.bookSourceUrl)) and    
      (LowerCase(A.loginUrl) = LowerCase(B.loginUrl)) and    
      (LowerCase(A.ruleBookContent) = LowerCase(B.ruleBookContent)) and    
      (LowerCase(A.httpUserAgent) = LowerCase(B.httpUserAgent)) and    
      (LowerCase(A.ruleBookKind) = LowerCase(B.ruleBookKind)) and    
      (LowerCase(A.ruleBookLastChapter) = LowerCase(B.ruleBookLastChapter)) and    
      (LowerCase(A.ruleBookName) = LowerCase(B.ruleBookName)) and    
      (LowerCase(A.ruleBookUrlPattern) = LowerCase(B.ruleBookUrlPattern)) and    
      (LowerCase(A.ruleChapterList) = LowerCase(B.ruleChapterList)) and    
      (LowerCase(A.ruleChapterName) = LowerCase(B.ruleChapterName)) and    
      (LowerCase(A.ruleChapterUrl) = LowerCase(B.ruleChapterUrl)) and    
      (LowerCase(A.ruleChapterUrlNext) = LowerCase(B.ruleChapterUrlNext)) and    
      (LowerCase(A.ruleContentUrl) = LowerCase(B.ruleContentUrl)) and    
      (LowerCase(A.ruleContentUrlNext) = LowerCase(B.ruleContentUrlNext)) and    
      (LowerCase(A.ruleCoverUrl) = LowerCase(B.ruleCoverUrl)) and    
      (LowerCase(A.ruleFindUrl) = LowerCase(B.ruleFindUrl)) and    
      (LowerCase(A.ruleIntroduce) = LowerCase(B.ruleIntroduce)) and    
      (LowerCase(A.ruleSearchAuthor) = LowerCase(B.ruleSearchAuthor)) and    
      (LowerCase(A.ruleSearchCoverUrl) = LowerCase(B.ruleSearchCoverUrl)) and    
      (LowerCase(A.ruleSearchKind) = LowerCase(B.ruleSearchKind)) and    
      (LowerCase(A.ruleSearchLastChapter) = LowerCase(B.ruleSearchLastChapter)) and    
      (LowerCase(A.ruleSearchList) = LowerCase(B.ruleSearchList)) and    
      (LowerCase(A.ruleSearchName) = LowerCase(B.ruleSearchName)) and    
      (LowerCase(A.ruleSearchNoteUrl) = LowerCase(B.ruleSearchNoteUrl)) and    
      (LowerCase(A.ruleSearchUrl) = LowerCase(B.ruleSearchUrl));
    if not CheckName then
      Result := Result and
        (LowerCase(A.bookSourceName) = LowerCase(B.bookSourceName)) and    
        (LowerCase(A.bookSourceGroup) = LowerCase(B.bookSourceGroup));    
  end;
  
var
  I, J, LastCount, ST: Integer;
  Item: TBookSourceItem;
  T: TProcessState;
  State: PProcessState;
begin
  I := 0;
  LastCount := FBookSrcData.Count;
  CheckName := Boolean(Integer(AJob.Data));
  
  T.STime := GetTimestamp;
  T.Min := 0;
  T.Value := 0;
  ST := 1000;
  
  try
    while I < FBookSrcData.Count do begin  
      Item := TBookSourceItem(FBookSrcData.O[I]);
      Inc(I);
      
      for J := FBookSrcData.Count - 1 downto I do begin
        if Equals(Item, TBookSourceItem(FBookSrcData.O[J])) then
          FBookSrcData.Remove(J);
      end;
      if AJob.IsTerminated or (FWaitStop > 0) then
        Break;
      if GetTimestamp - T.STime > ST then begin
        ST := 200;
        T.Value := I;
        T.Max := FBookSrcData.Count;
        New(State);
        State^ := T;
        State.NeedFree := True;
        Workers.Post(DoUpdateProcess, State, True);
      end;
    end;
  finally  
    if LastCount <> FBookSrcData.Count then
      Workers.Post(DoNotifyDataChange, nil, True);
    Sleep(100);
    Workers.Post(TaskFinish, nil, True);
  end; 
end;

procedure TForm1.RemoveSelected;
var
  I, V: Integer;
begin
  for I := SrcList.Count - 1 downto 0 do begin
    if SrcList.Selected[I] then begin
      V := FBookSrcData.IndexOfObject(JSONObject(FFilterList[I]));
      if V >= 0 then       
        FBookSrcData.Remove(V);
    end;
  end;
  NotifyListChange;  
end;

procedure TForm1.S1Click(Sender: TObject);
var
  S: string;
  Item: TBookSourceItem;
begin
  if (FCurIndex < 0) or (FCurIndex >= SrcList.Count) then Exit;
  Item := TBookSourceItem(FFilterList[FCurIndex]);
  if not Assigned(Item) then Exit;
  try
    FIsChange := False;
    S := Item.ToString();
    Item.Parse(EditData.Text);
    if not CheckItem(Item) then
      Item.Parse(S);
  finally
    NotifyListChange;
  end;
end;

procedure TForm1.S2Click(Sender: TObject);
begin             
  FBookSrcData.Sort(
    function (A, B: Pointer): Integer
    var
      Item1: PJSONValue absolute A;
      Item2: PJSONValue absolute B;
      S1, S2: string;
    begin
      if (Item1.FType = Item2.FType) and (Item1.FType = jdtObject) and 
        (Item1.AsJsonObject <> nil) and (Item2.AsJsonObject <> nil) 
      then begin
        S1 := TBookSourceItem(Item1.AsJsonObject).bookSourceName;
        S2 := TBookSourceItem(Item2.AsJsonObject).bookSourceName;
        Result := CompareStr(S1, S2);
      end else
        Result := 0;
    end
  );
  NotifyListChange(1);
end;

procedure TForm1.SpeedButton1Click(Sender: TObject);
begin
  edtLog.Lines.Clear;
end;

procedure TForm1.SrcListClick(Sender: TObject);
begin
  if SrcList.ItemIndex < 0 then Exit;
  if not CheckSaveState then Exit;
  FCurIndex := SrcList.ItemIndex;
  EditData.Text := TBookSourceItem(FFilterList[FCurIndex]).ToString(4);
  FIsChange := False;
end;

procedure TForm1.SrcListData(Control: TWinControl; Index: Integer;
  var Data: string);
var
  Item: TBookSourceItem;
begin
  if Index < FBookSrcData.Count then begin
    Item := TBookSourceItem(FFilterList[index]);
    Data := Format('【%s】%s', [Item.bookSourceGroup, Item.bookSourceName]);
  end else 
    Data := '';
end;

procedure TForm1.SrcListDblClick(Sender: TObject);
begin
  E1Click(E1);  
end;

procedure TForm1.SrcListKeyDown(Sender: TObject; var Key: Word;
  Shift: TShiftState);

  procedure PasteItems();
  var 
    pGlobal : Thandle;
  begin
    OpenClipboard(Handle);
    try
      pGlobal := GetClipboardData(CF_HDROP); //获取剪贴板的文件数据
      if pGlobal > 0 then
        AddSrcFiles(pGlobal);
    finally
      CloseClipboard;
    end;   
  end;
  
begin
  if Key = VK_DELETE then begin
    RemoveSelected();
  end else if Key = Ord('V') then begin  // 粘贴
    PasteItems();
  end;
end;

procedure TForm1.SrcListTextWndProc(var Message: TMessage);
begin
  if Message.Msg = WM_DROPFILES then
    WMDropFiles(TWMDropFiles(Message))
  else
    OldTextWndProc(Message);
end;

procedure TForm1.SrcListWndProc(var Message: TMessage);
begin
  if Message.Msg = WM_DROPFILES then
    WMDropFiles(TWMDropFiles(Message))
  else
    OldListWndProc(Message);
end;

procedure TForm1.T1Click(Sender: TObject);
var
  Item: TBookSourceItem;
  Msg: TStrings;
begin
  Item := TBookSourceItem(JSONObject.Create);
  try
    Item.Parse(EditData.Text);
    if CheckBookSourceItem(Item, True, edtLog.Lines) then
      LogD('恭喜, 检测通过!')
    else
      LogD('书源异常!');
  finally
    FreeAndNil(Item);
  end;
end;

procedure TForm1.TaskFinish(AJob: PJob);
var
  I: Integer;
begin
  I := AtomicDecrement(FTaskRef);
  if (I <= 0) or (FWaitStop > 0) then begin
    if (I = 0) and Assigned(Self) and (not (csDestroying in ComponentState)) then begin
      NotifyListChange();
      Button1Click(Button1);
    end;
  end else if not (csDestroying in ComponentState) then begin
    Log('正在校验书源...');       
    Workers.SendSignal(FWaitCheckBookSourceSingId);
  end;
end;

procedure TForm1.Timer1Timer(Sender: TObject);
begin
  DispLog;
end;

procedure TForm1.UpdateBookGroup(Item: TBookSourceItem);
var
  J: Integer;   
  ARef: Number;
  ABookGroup: TArray<string>;
  AGroup: string;
begin
  ABookGroup := Item.GetGroupList;

  for J := 0 to High(ABookGroup) do begin            
    ARef := 0;
    AGroup := Trim(ABookGroup[J]);
    FBookGroups.TryGetValue(AGroup, ARef);
    Inc(ARef);
    FBookGroups.AddOrUpdate(AGroup, ARef);
  end;
end;

procedure TForm1.W1Click(Sender: TObject);
begin
  EditData.WordWrap := not W1.Checked;
end;

procedure TForm1.W2Click(Sender: TObject);
begin
  ShellExecute(0, 'OPEN', PChar('http://www.cnblogs.com/yangyxd/'), nil, nil, SW_SHOWMAXIMIZED)
end;

procedure TForm1.WaitCheckBookSource(AJob: PJob);
var
  I, J: Integer;
begin
  if FBookSrcData.Count > 0 then begin
    FCheckCount := FBookSrcData.Count;
    FCurCheckIndex := 0;
    J := Min(FBookSrcData.Count, Workers.MaxWorkers - 1);
    for I := 0 to J - 1 do begin
      if AJob.IsTerminated then
        Break;
      Workers.Post(DoCheckBookSourceItem, nil);
    end;
  end else
    Workers.Post(TaskFinish, nil, True);
end;

procedure TForm1.WMDropFiles(var Msg: TWMDropFiles);
begin
  AddSrcFiles(Msg.Drop);
end;

procedure TForm1.X1Click(Sender: TObject);
begin
  EditData.CopyToClipboard;
  EditData.SelText := '';
end;

procedure TForm1.Z1Click(Sender: TObject);
begin
  EditData.Redo;
end;

end.
