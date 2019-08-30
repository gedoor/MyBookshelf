unit uFrmEditSource;

interface

uses
  uBookSourceBean, YxdJson,
  Winapi.Windows, Winapi.Messages, System.SysUtils, System.Variants, System.Classes, Vcl.Graphics,
  Vcl.Controls, Vcl.Forms, Vcl.Dialogs, Vcl.StdCtrls, Vcl.ExtCtrls;

type
  TNotifyEventA = reference to procedure (Item: TBookSourceItem);

  TfrmEditSource = class(TForm)
    ScrollBox1: TScrollBox;
    ComboBox1: TComboBox;
    Label1: TLabel;
    Label2: TLabel;
    Memo1: TMemo;
    Edit1: TEdit;
    Label3: TLabel;
    Label4: TLabel;
    Edit2: TEdit;
    Label5: TLabel;
    Edit3: TEdit;
    Label6: TLabel;
    Edit4: TEdit;
    Label7: TLabel;
    Edit5: TEdit;
    Label8: TLabel;
    Edit6: TEdit;
    Label9: TLabel;
    Edit7: TEdit;
    Label10: TLabel;
    Edit8: TEdit;
    Label11: TLabel;
    Edit9: TEdit;
    Label12: TLabel;
    Edit10: TEdit;
    Label13: TLabel;
    Edit11: TEdit;
    Label14: TLabel;
    Edit12: TEdit;
    Label15: TLabel;
    Edit13: TEdit;
    Label16: TLabel;
    Edit14: TEdit;
    Label17: TLabel;
    Edit15: TEdit;
    Label18: TLabel;
    Edit16: TEdit;
    Label19: TLabel;
    Edit17: TEdit;
    Label20: TLabel;
    Edit18: TEdit;
    Label21: TLabel;
    Edit19: TEdit;
    Label22: TLabel;
    Edit20: TEdit;
    Label23: TLabel;
    Edit21: TEdit;
    Label24: TLabel;
    Edit22: TEdit;
    Label25: TLabel;
    Edit23: TEdit;
    Label26: TLabel;
    Edit24: TEdit;
    Label27: TLabel;
    Edit25: TEdit;
    Edit26: TEdit;
    Label28: TLabel;
    Panel1: TPanel;
    Button1: TButton;
    Button2: TButton;
    CheckBox1: TCheckBox;
    Label29: TLabel;
    Edit27: TEdit;
    Label30: TLabel;
    Edit28: TEdit;
    Button3: TButton;
    procedure FormShow(Sender: TObject);
    procedure Button1Click(Sender: TObject);
    procedure FormResize(Sender: TObject);
    procedure FormClose(Sender: TObject; var Action: TCloseAction);
    procedure Button2Click(Sender: TObject);
    procedure FormCreate(Sender: TObject);
    procedure Button3Click(Sender: TObject);
    procedure FormMouseWheel(Sender: TObject; Shift: TShiftState;
      WheelDelta: Integer; MousePos: TPoint; var Handled: Boolean);
  private
    { Private declarations }
    FDisableChange: Boolean;
  public
    { Public declarations }
    Data: TBookSourceItem;
    CallBack: TNotifyEventA;

    procedure ApplayEdit(Data: TBookSourceItem);
  end;

var
  frmEditSource: TfrmEditSource;

procedure ShowEditSource(Item: TBookSourceItem; CallBack: TNotifyEventA = nil);

implementation

{$R *.dfm}

uses
  uFrmMain, Math;

var
  LastW, LastH: Integer;

procedure ShowEditSource(Item: TBookSourceItem; CallBack: TNotifyEventA);
var
  F: TfrmEditSource;
begin
  F := TfrmEditSource.Create(Application);
  F.Data := Item;
  F.CallBack := CallBack;
  F.Show;
end;

procedure TfrmEditSource.ApplayEdit(Data: TBookSourceItem);
var
  I: Integer;
  Item: TControl;
begin
  Data.enable := CheckBox1.Checked;
  Data.weight := StrToIntDef(Edit27.Text, Data.weight);
  Data.serialNumber := StrToIntDef(Edit28.Text, Data.serialNumber);
  for I := 0 to ScrollBox1.ControlCount - 1 do begin
    Item := ScrollBox1.Controls[I];
    if not Item.Visible then Continue;
    if Item.Hint = '' then Continue;

    if Item is TEdit then
      Data.S[Item.Hint] := TEdit(Item).Text
    else if Item is TComboBox then
      Data.S[Item.Hint] := TComboBox(Item).Text
    else if Item is TMemo then
      Data.S[Item.Hint] := TMemo(Item).Text;
  end;
end;

procedure TfrmEditSource.Button1Click(Sender: TObject);
begin
  FDisableChange := True;
  try
    ApplayEdit(Data);
  finally
    FDisableChange := False;
  end;

  if Assigned(CallBack) then
    CallBack(Data);
  Close;
end;

procedure TfrmEditSource.Button2Click(Sender: TObject);
begin
  Close;
end;

procedure TfrmEditSource.Button3Click(Sender: TObject);
var
  Item: TBookSourceItem;
  Msg: TStrings;
begin
  Msg := TStringList.Create;
  Item := TBookSourceItem(JSONObject.Create);
  try
    Item.Parse(Data.ToString());
    ApplayEdit(Item);
    if Form1.CheckBookSourceItem(Item, True, Msg) then
      MessageBox(Handle, PChar(Msg.Text), '恭喜, 检测通过!', 64)
    else
      MessageBox(Handle, PChar(Msg.Text), '书源异常', 48)
  finally
    FreeAndNil(Item);
    FreeAndNil(Msg);
  end;
end;

procedure TfrmEditSource.FormClose(Sender: TObject; var Action: TCloseAction);
begin
  Action := caFree;
end;

procedure TfrmEditSource.FormCreate(Sender: TObject);
begin
  if LastW <= 0 then LastW := Self.Width;
  if LastH <= 0 then LastH := Self.Height;
  Self.SetBounds(Left, Top, LastW, LastH);
end;

procedure TfrmEditSource.FormMouseWheel(Sender: TObject; Shift: TShiftState;
  WheelDelta: Integer; MousePos: TPoint; var Handled: Boolean);
begin
  if WheelDelta < 0 then
    ScrollBox1.Perform(WM_VSCROLL,SB_LINEDOWN,0)
  else
    ScrollBox1.Perform(WM_VSCROLL,SB_LINEUP,0);
end;

procedure TfrmEditSource.FormResize(Sender: TObject);
begin
  LastW := Width;
  LastH := Height;
end;

procedure TfrmEditSource.FormShow(Sender: TObject);
var
  I: Integer;
  Item: TControl;
begin
  ComboBox1.Items := Form1.bookGroupList.Items;

  if Assigned(Data) then begin
    FDisableChange := True;
    try
      CheckBox1.Checked := Data.enable;
      Edit27.Text := IntToStr(Data.weight);
      Edit28.Text := IntToStr(Data.serialNumber);
      for I := 0 to ScrollBox1.ControlCount - 1 do begin
        Item := ScrollBox1.Controls[I];
        if not Item.Visible then Continue;
        if Item.Hint = '' then Continue;

        if Item is TEdit then
          TEdit(Item).Text := Data.S[Item.Hint]
        else if Item is TComboBox then
          TComboBox(Item).Text := Data.S[Item.Hint]
        else if Item is TMemo then
          TMemo(Item).Text := Data.S[Item.Hint];
      end;
    finally
      FDisableChange := False;
    end;
  end;
end;

end.
