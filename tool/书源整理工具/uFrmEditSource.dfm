object frmEditSource: TfrmEditSource
  Left = 0
  Top = 0
  BorderIcons = [biSystemMenu]
  Caption = #32534#36753#20070#28304
  ClientHeight = 743
  ClientWidth = 1198
  Color = clBtnFace
  Font.Charset = DEFAULT_CHARSET
  Font.Color = clWindowText
  Font.Height = -12
  Font.Name = 'Courier New'
  Font.Style = []
  OldCreateOrder = False
  Position = poScreenCenter
  OnClose = FormClose
  OnCreate = FormCreate
  OnMouseWheel = FormMouseWheel
  OnResize = FormResize
  OnShow = FormShow
  PixelsPerInch = 96
  TextHeight = 15
  object ScrollBox1: TScrollBox
    Left = 0
    Top = 0
    Width = 1198
    Height = 700
    HorzScrollBar.Smooth = True
    HorzScrollBar.Style = ssFlat
    VertScrollBar.Smooth = True
    VertScrollBar.Style = ssFlat
    VertScrollBar.Tracking = True
    Align = alClient
    BevelInner = bvNone
    BevelOuter = bvNone
    BorderStyle = bsNone
    Color = clWindow
    Padding.Bottom = 8
    ParentColor = False
    TabOrder = 0
    DesignSize = (
      1181
      700)
    object Label1: TLabel
      Left = 603
      Top = 13
      Width = 184
      Height = 12
      Caption = #20998#32452#21517#31216#65306'(bookSourceGroup)'
      Font.Charset = DEFAULT_CHARSET
      Font.Color = clWindowText
      Font.Height = -12
      Font.Name = #23435#20307
      Font.Style = [fsBold]
      ParentFont = False
    end
    object Label2: TLabel
      Left = 8
      Top = 679
      Width = 156
      Height = 12
      Caption = #21457#29616#35268#21017#65306'(ruleFindUrl)'
      Font.Charset = DEFAULT_CHARSET
      Font.Color = clWindowText
      Font.Height = -12
      Font.Name = #23435#20307
      Font.Style = [fsBold]
      ParentFont = False
    end
    object Label3: TLabel
      Left = 8
      Top = 13
      Width = 177
      Height = 12
      Caption = #20070#28304#21517#31216#65306'(bookSourceName)'
      Font.Charset = DEFAULT_CHARSET
      Font.Color = clRed
      Font.Height = -12
      Font.Name = #23435#20307
      Font.Style = [fsBold]
      ParentFont = False
    end
    object Label4: TLabel
      Left = 8
      Top = 40
      Width = 165
      Height = 12
      Caption = #20070#28304'URL'#65306'(bookSourceUrl)'
      Font.Charset = DEFAULT_CHARSET
      Font.Color = clRed
      Font.Height = -12
      Font.Name = #23435#20307
      Font.Style = [fsBold]
      ParentFont = False
    end
    object Label5: TLabel
      Left = 8
      Top = 66
      Width = 114
      Height = 12
      Caption = #30331#24405'URL'#65306'(loginUrl)'
      Font.Charset = DEFAULT_CHARSET
      Font.Color = clTeal
      Font.Height = -12
      Font.Name = #23435#20307
      Font.Style = []
      ParentFont = False
    end
    object Label6: TLabel
      Left = 8
      Top = 91
      Width = 170
      Height = 12
      Caption = #25628#32034#22320#22336#65306'(ruleSearchUrl)'
      Font.Charset = DEFAULT_CHARSET
      Font.Color = clGreen
      Font.Height = -12
      Font.Name = #23435#20307
      Font.Style = [fsBold]
      ParentFont = False
    end
    object Label7: TLabel
      Left = 8
      Top = 117
      Width = 229
      Height = 12
      Caption = #25628#32034#32467#26524#21015#34920#35268#21017#65306'(ruleSearchList)'
      Font.Charset = DEFAULT_CHARSET
      Font.Color = clGreen
      Font.Height = -12
      Font.Name = #23435#20307
      Font.Style = [fsBold]
      ParentFont = False
    end
    object Label8: TLabel
      Left = 8
      Top = 142
      Width = 229
      Height = 12
      Caption = #25628#32034#32467#26524#20070#21517#35268#21017#65306'(ruleSearchName)'
      Font.Charset = DEFAULT_CHARSET
      Font.Color = clGreen
      Font.Height = -12
      Font.Name = #23435#20307
      Font.Style = [fsBold]
      ParentFont = False
    end
    object Label9: TLabel
      Left = 8
      Top = 168
      Width = 243
      Height = 12
      Caption = #25628#32034#32467#26524#20316#32773#35268#21017#65306'(ruleSearchAuthor)'
      Font.Charset = DEFAULT_CHARSET
      Font.Color = clGreen
      Font.Height = -12
      Font.Name = #23435#20307
      Font.Style = [fsBold]
      ParentFont = False
    end
    object Label10: TLabel
      Left = 8
      Top = 193
      Width = 229
      Height = 12
      Caption = #25628#32034#32467#26524#20998#31867#35268#21017#65306'(ruleSearchKind)'
      Font.Charset = DEFAULT_CHARSET
      Font.Color = clGreen
      Font.Height = -12
      Font.Name = #23435#20307
      Font.Style = [fsBold]
      ParentFont = False
    end
    object Label11: TLabel
      Left = 8
      Top = 219
      Width = 270
      Height = 12
      Caption = #25628#32034#32467#26524#26368#26032#31456#33410#35268#21017#65306'(ruleSearchLastChapter)'
      Font.Charset = DEFAULT_CHARSET
      Font.Color = clGreen
      Font.Height = -12
      Font.Name = #23435#20307
      Font.Style = []
      ParentFont = False
    end
    object Label12: TLabel
      Left = 8
      Top = 244
      Width = 257
      Height = 12
      Caption = #25628#32034#32467#26524#23553#38754#35268#21017#65306'(ruleSearchCoverUrl)'
      Font.Charset = DEFAULT_CHARSET
      Font.Color = clGreen
      Font.Height = -12
      Font.Name = #23435#20307
      Font.Style = [fsBold]
      ParentFont = False
    end
    object Label13: TLabel
      Left = 8
      Top = 270
      Width = 271
      Height = 12
      Caption = #25628#32034#32467#26524#20070#31821'URL'#35268#21017#65306'(ruleSearchNoteUrl)'
      Font.Charset = DEFAULT_CHARSET
      Font.Color = clGreen
      Font.Height = -12
      Font.Name = #23435#20307
      Font.Style = [fsBold]
      ParentFont = False
    end
    object Label14: TLabel
      Left = 8
      Top = 295
      Width = 222
      Height = 12
      Caption = #20070#31821#35814#24773'URL'#27491#21017#65306'(ruleBookUrlPattern)'
      Font.Charset = DEFAULT_CHARSET
      Font.Color = clWindowText
      Font.Height = -12
      Font.Name = #23435#20307
      Font.Style = []
      ParentFont = False
    end
    object Label15: TLabel
      Left = 8
      Top = 321
      Width = 144
      Height = 12
      Caption = #20070#21517#35268#21017#65306'(ruleBookName)'
      Font.Charset = DEFAULT_CHARSET
      Font.Color = clWindowText
      Font.Height = -12
      Font.Name = #23435#20307
      Font.Style = []
      ParentFont = False
    end
    object Label16: TLabel
      Left = 8
      Top = 346
      Width = 156
      Height = 12
      Caption = #20316#32773#35268#21017#65306'(ruleBookAuthor)'
      Font.Charset = DEFAULT_CHARSET
      Font.Color = clWindowText
      Font.Height = -12
      Font.Name = #23435#20307
      Font.Style = []
      ParentFont = False
    end
    object Label17: TLabel
      Left = 8
      Top = 372
      Width = 144
      Height = 12
      Caption = #23553#38754#35268#21017#65306'(ruleCoverUrl)'
      Font.Charset = DEFAULT_CHARSET
      Font.Color = clWindowText
      Font.Height = -12
      Font.Name = #23435#20307
      Font.Style = []
      ParentFont = False
    end
    object Label18: TLabel
      Left = 8
      Top = 397
      Width = 144
      Height = 12
      Caption = #20998#31867#35268#21017#65306'(ruleBookKind)'
      Font.Charset = DEFAULT_CHARSET
      Font.Color = clWindowText
      Font.Height = -12
      Font.Name = #23435#20307
      Font.Style = []
      ParentFont = False
    end
    object Label19: TLabel
      Left = 8
      Top = 423
      Width = 210
      Height = 12
      Caption = #26368#26032#31456#33410#35268#21017#65306'(ruleBookLastChapter)'
      Font.Charset = DEFAULT_CHARSET
      Font.Color = clWindowText
      Font.Height = -12
      Font.Name = #23435#20307
      Font.Style = []
      ParentFont = False
    end
    object Label20: TLabel
      Left = 8
      Top = 448
      Width = 170
      Height = 12
      Caption = #31616#20171#35268#21017#65306'(ruleIntroduce)'
      Font.Charset = DEFAULT_CHARSET
      Font.Color = clWindowText
      Font.Height = -12
      Font.Name = #23435#20307
      Font.Style = [fsBold]
      ParentFont = False
    end
    object Label21: TLabel
      Left = 8
      Top = 474
      Width = 198
      Height = 12
      Caption = #30446#24405'URL'#35268#21017#65306'(ruleChapterUrl)'
      Font.Charset = DEFAULT_CHARSET
      Font.Color = clWindowText
      Font.Height = -12
      Font.Name = #23435#20307
      Font.Style = [fsBold]
      ParentFont = False
    end
    object Label22: TLabel
      Left = 8
      Top = 499
      Width = 210
      Height = 12
      Caption = #30446#24405#21015#34920#35268#21017#65306'(ruleChapterList)'
      Font.Charset = DEFAULT_CHARSET
      Font.Color = clWindowText
      Font.Height = -12
      Font.Name = #23435#20307
      Font.Style = [fsBold]
      ParentFont = False
    end
    object Label23: TLabel
      Left = 8
      Top = 525
      Width = 234
      Height = 12
      Caption = #30446#24405#19979#19968#39029'URL'#35268#21017#65306'(ruleChapterUrlNext)'
      Font.Charset = DEFAULT_CHARSET
      Font.Color = clWindowText
      Font.Height = -12
      Font.Name = #23435#20307
      Font.Style = []
      ParentFont = False
    end
    object Label24: TLabel
      Left = 8
      Top = 550
      Width = 210
      Height = 12
      Caption = #31456#33410#21517#31216#35268#21017#65306'(ruleChapterName)'
      Font.Charset = DEFAULT_CHARSET
      Font.Color = clNavy
      Font.Height = -12
      Font.Name = #23435#20307
      Font.Style = [fsBold]
      ParentFont = False
    end
    object Label25: TLabel
      Left = 8
      Top = 576
      Width = 224
      Height = 12
      Caption = #27491#25991#31456#33410'URL'#35268#21017#65306'(ruleContentUrl)'
      Font.Charset = DEFAULT_CHARSET
      Font.Color = clWindowText
      Font.Height = -12
      Font.Name = #23435#20307
      Font.Style = [fsBold]
      ParentFont = False
    end
    object Label26: TLabel
      Left = 8
      Top = 601
      Width = 234
      Height = 12
      Caption = #27491#25991#19979#19968#39029'URL'#35268#21017#65306'(ruleContentUrlNext)'
      Font.Charset = DEFAULT_CHARSET
      Font.Color = clWindowText
      Font.Height = -12
      Font.Name = #23435#20307
      Font.Style = []
      ParentFont = False
    end
    object Label27: TLabel
      Left = 8
      Top = 627
      Width = 184
      Height = 12
      Caption = #27491#25991#35268#21017#65306'(ruleBookContent)'
      Font.Charset = DEFAULT_CHARSET
      Font.Color = clNavy
      Font.Height = -12
      Font.Name = #23435#20307
      Font.Style = [fsBold]
      ParentFont = False
    end
    object Label28: TLabel
      Left = 9
      Top = 653
      Width = 162
      Height = 12
      Caption = #27983#35272#22120#27169#25311#65306'(HttpUserAgent)'
      Font.Charset = DEFAULT_CHARSET
      Font.Color = clTeal
      Font.Height = -12
      Font.Name = #23435#20307
      Font.Style = []
      ParentFont = False
    end
    object ComboBox1: TComboBox
      Left = 793
      Top = 8
      Width = 383
      Height = 23
      Hint = 'bookSourceGroup'
      Anchors = [akLeft, akTop, akRight]
      DropDownCount = 20
      TabOrder = 1
    end
    object Memo1: TMemo
      Left = 296
      Top = 674
      Width = 881
      Height = 200
      Hint = 'ruleFindUrl'
      Margins.Bottom = 8
      Anchors = [akLeft, akTop, akRight]
      ScrollBars = ssVertical
      TabOrder = 27
    end
    object Edit1: TEdit
      Left = 295
      Top = 8
      Width = 290
      Height = 23
      Hint = 'bookSourceName'
      TabOrder = 0
      TextHint = #24517#22635
    end
    object Edit2: TEdit
      Left = 295
      Top = 35
      Width = 881
      Height = 23
      Hint = 'bookSourceUrl'
      Anchors = [akLeft, akTop, akRight]
      TabOrder = 2
      TextHint = #24517#22635
    end
    object Edit3: TEdit
      Left = 295
      Top = 61
      Width = 881
      Height = 23
      Hint = 'loginUrl'
      Anchors = [akLeft, akTop, akRight]
      TabOrder = 3
    end
    object Edit4: TEdit
      Left = 295
      Top = 86
      Width = 881
      Height = 23
      Hint = 'ruleSearchUrl'
      Anchors = [akLeft, akTop, akRight]
      TabOrder = 4
    end
    object Edit5: TEdit
      Left = 295
      Top = 112
      Width = 881
      Height = 23
      Hint = 'ruleSearchList'
      Anchors = [akLeft, akTop, akRight]
      TabOrder = 5
    end
    object Edit6: TEdit
      Left = 295
      Top = 137
      Width = 881
      Height = 23
      Hint = 'ruleSearchName'
      Anchors = [akLeft, akTop, akRight]
      TabOrder = 6
    end
    object Edit7: TEdit
      Left = 295
      Top = 163
      Width = 881
      Height = 23
      Hint = 'ruleSearchAuthor'
      Anchors = [akLeft, akTop, akRight]
      TabOrder = 7
    end
    object Edit8: TEdit
      Left = 295
      Top = 188
      Width = 881
      Height = 23
      Hint = 'ruleSearchKind'
      Anchors = [akLeft, akTop, akRight]
      TabOrder = 8
    end
    object Edit9: TEdit
      Left = 295
      Top = 214
      Width = 881
      Height = 23
      Hint = 'ruleSearchLastChapter'
      Anchors = [akLeft, akTop, akRight]
      TabOrder = 9
    end
    object Edit10: TEdit
      Left = 295
      Top = 239
      Width = 881
      Height = 23
      Hint = 'ruleSearchCoverUrl'
      Anchors = [akLeft, akTop, akRight]
      TabOrder = 10
    end
    object Edit11: TEdit
      Left = 295
      Top = 265
      Width = 881
      Height = 23
      Hint = 'ruleSearchNoteUrl'
      Anchors = [akLeft, akTop, akRight]
      TabOrder = 11
    end
    object Edit12: TEdit
      Left = 295
      Top = 290
      Width = 881
      Height = 23
      Hint = 'ruleBookUrlPattern'
      Anchors = [akLeft, akTop, akRight]
      TabOrder = 12
    end
    object Edit13: TEdit
      Left = 295
      Top = 316
      Width = 881
      Height = 23
      Hint = 'ruleBookName'
      Anchors = [akLeft, akTop, akRight]
      TabOrder = 13
    end
    object Edit14: TEdit
      Left = 295
      Top = 341
      Width = 881
      Height = 23
      Hint = 'ruleBookAuthor'
      Anchors = [akLeft, akTop, akRight]
      TabOrder = 14
    end
    object Edit15: TEdit
      Left = 295
      Top = 367
      Width = 881
      Height = 23
      Hint = 'ruleCoverUrl'
      Anchors = [akLeft, akTop, akRight]
      TabOrder = 15
    end
    object Edit16: TEdit
      Left = 295
      Top = 392
      Width = 881
      Height = 23
      Hint = 'ruleBookKind'
      Anchors = [akLeft, akTop, akRight]
      TabOrder = 16
    end
    object Edit17: TEdit
      Left = 295
      Top = 418
      Width = 881
      Height = 23
      Hint = 'ruleBookLastChapter'
      Anchors = [akLeft, akTop, akRight]
      TabOrder = 17
    end
    object Edit18: TEdit
      Left = 295
      Top = 443
      Width = 881
      Height = 23
      Hint = 'ruleIntroduce'
      Anchors = [akLeft, akTop, akRight]
      TabOrder = 18
    end
    object Edit19: TEdit
      Left = 295
      Top = 469
      Width = 881
      Height = 23
      Hint = 'ruleChapterUrl'
      Anchors = [akLeft, akTop, akRight]
      TabOrder = 19
    end
    object Edit20: TEdit
      Left = 295
      Top = 494
      Width = 881
      Height = 23
      Hint = 'ruleChapterList'
      Anchors = [akLeft, akTop, akRight]
      TabOrder = 20
    end
    object Edit21: TEdit
      Left = 295
      Top = 520
      Width = 881
      Height = 23
      Hint = 'ruleChapterUrlNext'
      Anchors = [akLeft, akTop, akRight]
      TabOrder = 21
    end
    object Edit22: TEdit
      Left = 295
      Top = 545
      Width = 881
      Height = 23
      Hint = 'ruleChapterName'
      Anchors = [akLeft, akTop, akRight]
      TabOrder = 22
    end
    object Edit23: TEdit
      Left = 295
      Top = 571
      Width = 881
      Height = 23
      Hint = 'ruleContentUrl'
      Anchors = [akLeft, akTop, akRight]
      TabOrder = 23
    end
    object Edit24: TEdit
      Left = 295
      Top = 596
      Width = 881
      Height = 23
      Hint = 'ruleContentUrlNext'
      Anchors = [akLeft, akTop, akRight]
      TabOrder = 24
    end
    object Edit25: TEdit
      Left = 295
      Top = 622
      Width = 881
      Height = 23
      Hint = 'ruleBookContent'
      Anchors = [akLeft, akTop, akRight]
      TabOrder = 25
    end
    object Edit26: TEdit
      Left = 296
      Top = 648
      Width = 881
      Height = 23
      Hint = 'httpUserAgent'
      Anchors = [akLeft, akTop, akRight]
      TabOrder = 26
    end
  end
  object Panel1: TPanel
    Left = 0
    Top = 700
    Width = 1198
    Height = 43
    Align = alBottom
    BevelOuter = bvNone
    Font.Charset = DEFAULT_CHARSET
    Font.Color = clWindowText
    Font.Height = -12
    Font.Name = #23435#20307
    Font.Style = []
    ParentFont = False
    ShowCaption = False
    TabOrder = 1
    DesignSize = (
      1198
      43)
    object Label29: TLabel
      Left = 140
      Top = 19
      Width = 36
      Height = 12
      Caption = #26435#37325#65306
      Font.Charset = DEFAULT_CHARSET
      Font.Color = clTeal
      Font.Height = -12
      Font.Name = #23435#20307
      Font.Style = []
      ParentFont = False
    end
    object Label30: TLabel
      Left = 291
      Top = 19
      Width = 48
      Height = 12
      Caption = #24207#21015#21495#65306
      Font.Charset = DEFAULT_CHARSET
      Font.Color = clTeal
      Font.Height = -12
      Font.Name = #23435#20307
      Font.Style = []
      ParentFont = False
    end
    object Button1: TButton
      Left = 1077
      Top = 10
      Width = 107
      Height = 25
      Anchors = [akTop, akRight]
      Caption = #30830#23450'(&O)'
      Default = True
      TabOrder = 0
      OnClick = Button1Click
    end
    object Button2: TButton
      Left = 949
      Top = 10
      Width = 107
      Height = 25
      Anchors = [akTop, akRight]
      Cancel = True
      Caption = #21462#28040'(&C)'
      ModalResult = 2
      TabOrder = 1
      OnClick = Button2Click
    end
    object CheckBox1: TCheckBox
      Left = 12
      Top = 16
      Width = 113
      Height = 17
      Caption = #21551#29992#20070#28304
      TabOrder = 2
    end
    object Edit27: TEdit
      Left = 178
      Top = 14
      Width = 87
      Height = 20
      Hint = 'weight'
      NumbersOnly = True
      ParentShowHint = False
      ShowHint = True
      TabOrder = 3
      Text = '0'
    end
    object Edit28: TEdit
      Left = 338
      Top = 14
      Width = 87
      Height = 20
      Hint = 'weight'
      NumbersOnly = True
      ParentShowHint = False
      ShowHint = True
      TabOrder = 4
      Text = '0'
    end
    object Button3: TButton
      Left = 793
      Top = 10
      Width = 107
      Height = 25
      Anchors = [akTop, akRight]
      Caption = #27979#35797#20070#28304'(&T)'
      ModalResult = 2
      TabOrder = 5
      OnClick = Button3Click
    end
  end
end
