object Form1: TForm1
  Left = 0
  Top = 0
  Caption = #38405#35835#20070#28304#25972#29702#24037#20855
  ClientHeight = 548
  ClientWidth = 1145
  Color = 15921906
  DoubleBuffered = True
  Font.Charset = DEFAULT_CHARSET
  Font.Color = clWindowText
  Font.Height = -11
  Font.Name = 'Tahoma'
  Font.Pitch = fpFixed
  Font.Style = []
  Menu = MainMenu1
  OldCreateOrder = False
  OnCreate = FormCreate
  OnDestroy = FormDestroy
  OnShow = FormShow
  PixelsPerInch = 96
  TextHeight = 13
  object Splitter1: TSplitter
    AlignWithMargins = True
    Left = 313
    Top = 37
    Width = 4
    Height = 480
    Margins.Left = 0
    Margins.Top = 37
    Margins.Right = 0
    Margins.Bottom = 0
    Color = clSilver
    ParentColor = False
    ExplicitLeft = 257
    ExplicitTop = 0
    ExplicitHeight = 697
  end
  object Panel1: TPanel
    Left = 0
    Top = 0
    Width = 313
    Height = 517
    Align = alLeft
    BevelOuter = bvNone
    Padding.Left = 4
    Padding.Top = 4
    Padding.Bottom = 4
    TabOrder = 1
    object SrcList: TListBox
      AlignWithMargins = True
      Left = 4
      Top = 38
      Width = 309
      Height = 475
      Hint = #23558#20070#28304#25991#20214#25302#20837#27492#22788
      Margins.Left = 0
      Margins.Top = 2
      Margins.Right = 0
      Margins.Bottom = 0
      Style = lbVirtual
      Align = alClient
      BorderStyle = bsNone
      DoubleBuffered = True
      Font.Charset = DEFAULT_CHARSET
      Font.Color = clWindowText
      Font.Height = -12
      Font.Name = #23435#20307
      Font.Pitch = fpFixed
      Font.Style = []
      ItemHeight = 18
      MultiSelect = True
      ParentDoubleBuffered = False
      ParentFont = False
      ParentShowHint = False
      PopupMenu = PopupMenu1
      ShowHint = True
      TabOrder = 0
      OnClick = SrcListClick
      OnData = SrcListData
      OnDblClick = SrcListDblClick
      OnKeyDown = SrcListKeyDown
    end
    object Panel4: TPanel
      Left = 4
      Top = 4
      Width = 309
      Height = 32
      Align = alTop
      BevelOuter = bvNone
      DoubleBuffered = True
      ParentDoubleBuffered = False
      TabOrder = 1
      DesignSize = (
        309
        32)
      object lbCount: TLabel
        Left = 0
        Top = 7
        Width = 60
        Height = 13
        Caption = #20070#28304#21015#34920#65306
        Transparent = False
      end
      object bookGroupList: TComboBox
        Left = 80
        Top = 3
        Width = 223
        Height = 21
        Anchors = [akLeft, akTop, akRight]
        DropDownCount = 24
        Font.Charset = GB2312_CHARSET
        Font.Color = clWindowText
        Font.Height = -11
        Font.Name = 'Tahoma'
        Font.Pitch = fpFixed
        Font.Style = []
        ParentFont = False
        TabOrder = 0
        OnChange = bookGroupListChange
        OnClick = bookGroupListChange
      end
    end
    object StaticText1: TStaticText
      AlignWithMargins = True
      Left = 7
      Top = 66
      Width = 303
      Height = 444
      Margins.Top = 30
      Align = alClient
      Alignment = taCenter
      Caption = #40736#26631#25302#20837#20070#28304#25991#20214#21040#27492#22788
      Color = clWindow
      Font.Charset = DEFAULT_CHARSET
      Font.Color = 6736896
      Font.Height = -16
      Font.Name = #24494#36719#38597#40657
      Font.Pitch = fpFixed
      Font.Style = []
      ParentColor = False
      ParentFont = False
      PopupMenu = PopupMenu1
      TabOrder = 2
      Transparent = False
      ExplicitLeft = 4
      ExplicitTop = 36
      ExplicitWidth = 309
      ExplicitHeight = 477
    end
  end
  object Panel2: TPanel
    Left = 317
    Top = 0
    Width = 828
    Height = 517
    Align = alClient
    BevelOuter = bvNone
    ParentBackground = False
    ParentColor = True
    TabOrder = 0
    object Splitter2: TSplitter
      Left = 0
      Top = 372
      Width = 828
      Height = 4
      Cursor = crVSplit
      Align = alBottom
      Color = clSilver
      ParentColor = False
      ExplicitTop = 373
    end
    object Panel3: TPanel
      Left = 0
      Top = 0
      Width = 828
      Height = 36
      Align = alTop
      BevelOuter = bvNone
      TabOrder = 1
      object Label1: TLabel
        Left = 144
        Top = 10
        Width = 64
        Height = 13
        Caption = #24037#20316#32447#31243#25968':'
        Transparent = False
      end
      object Button1: TButton
        Left = 6
        Top = 6
        Width = 128
        Height = 24
        Caption = #24320#22987#22788#29702'(&B)'
        TabOrder = 0
        OnClick = Button1Click
      end
      object Edit1: TEdit
        Left = 212
        Top = 7
        Width = 35
        Height = 21
        Alignment = taCenter
        NumbersOnly = True
        TabOrder = 1
        Text = '60'
      end
      object CheckBox1: TCheckBox
        Left = 264
        Top = 9
        Width = 97
        Height = 17
        Caption = #21435#38500#37325#22797
        Checked = True
        State = cbChecked
        TabOrder = 2
      end
      object CheckBox3: TCheckBox
        Left = 344
        Top = 9
        Width = 97
        Height = 17
        Hint = #21435#37325#26102#19981#26816#27979#20070#28304#21517#31216#21644#20998#32452
        Caption = #30495#23454#21435#37325
        Checked = True
        State = cbChecked
        TabOrder = 4
      end
      object CheckBox2: TCheckBox
        Left = 429
        Top = 9
        Width = 97
        Height = 17
        Caption = #26657#39564#20070#28304
        TabOrder = 3
      end
    end
    object EditData: TSynMemo
      Left = 0
      Top = 36
      Width = 828
      Height = 336
      Align = alClient
      Ctl3D = True
      ParentCtl3D = False
      Font.Charset = DEFAULT_CHARSET
      Font.Color = clWindowText
      Font.Height = -13
      Font.Name = 'Courier New'
      Font.Style = []
      PopupMenu = PopupMenu2
      TabOrder = 0
      CodeFolding.GutterShapeSize = 11
      CodeFolding.CollapsedLineColor = clGrayText
      CodeFolding.FolderBarLinesColor = clGrayText
      CodeFolding.IndentGuidesColor = clGray
      CodeFolding.IndentGuides = True
      CodeFolding.ShowCollapsedLine = False
      CodeFolding.ShowHintMark = True
      UseCodeFolding = False
      BorderStyle = bsNone
      Gutter.AutoSize = True
      Gutter.BorderStyle = gbsNone
      Gutter.Color = cl3DLight
      Gutter.BorderColor = clWindowFrame
      Gutter.Font.Charset = DEFAULT_CHARSET
      Gutter.Font.Color = clWindowText
      Gutter.Font.Height = -11
      Gutter.Font.Name = 'Courier New'
      Gutter.Font.Style = []
      Gutter.ShowLineNumbers = True
      Highlighter = SynJSONSyn1
      WordWrap = True
      OnChange = EditDataChange
      FontSmoothing = fsmNone
      ExplicitHeight = 481
    end
    object Panel5: TPanel
      Left = 0
      Top = 376
      Width = 828
      Height = 141
      Align = alBottom
      BevelOuter = bvNone
      TabOrder = 2
      ExplicitLeft = 2
      DesignSize = (
        828
        141)
      object Label2: TLabel
        Left = 6
        Top = 2
        Width = 36
        Height = 13
        Caption = #26085#24535#65306
        Transparent = False
      end
      object SpeedButton1: TSpeedButton
        Left = 783
        Top = 0
        Width = 43
        Height = 20
        Anchors = [akTop, akRight]
        Caption = #28165#31354
        Flat = True
        Font.Charset = DEFAULT_CHARSET
        Font.Color = clTeal
        Font.Height = -11
        Font.Name = 'Tahoma'
        Font.Pitch = fpFixed
        Font.Style = []
        ParentFont = False
        OnClick = SpeedButton1Click
      end
      object edtLog: TSynMemo
        AlignWithMargins = True
        Left = 0
        Top = 20
        Width = 828
        Height = 121
        Margins.Left = 0
        Margins.Top = 20
        Margins.Right = 0
        Margins.Bottom = 0
        Align = alClient
        Ctl3D = False
        ParentCtl3D = False
        Font.Charset = DEFAULT_CHARSET
        Font.Color = clWindowText
        Font.Height = -12
        Font.Name = 'Courier New'
        Font.Style = []
        TabOrder = 0
        CodeFolding.GutterShapeSize = 11
        CodeFolding.CollapsedLineColor = clGrayText
        CodeFolding.FolderBarLinesColor = clGrayText
        CodeFolding.IndentGuidesColor = clGray
        CodeFolding.IndentGuides = False
        CodeFolding.ShowCollapsedLine = False
        CodeFolding.ShowHintMark = True
        UseCodeFolding = False
        BookMarkOptions.EnableKeys = False
        BookMarkOptions.GlyphsVisible = False
        BorderStyle = bsNone
        Gutter.AutoSize = True
        Gutter.BorderStyle = gbsNone
        Gutter.Color = cl3DLight
        Gutter.BorderColor = clWindowFrame
        Gutter.Font.Charset = DEFAULT_CHARSET
        Gutter.Font.Color = clWindowText
        Gutter.Font.Height = -11
        Gutter.Font.Name = 'Courier New'
        Gutter.Font.Style = []
        Gutter.ShowLineNumbers = True
        Gutter.Visible = False
        Gutter.Width = 0
        Options = [eoScrollPastEol, eoShowScrollHint, eoSmartTabDelete, eoSmartTabs, eoTabsToSpaces]
        ReadOnly = True
        RightEdge = 0
        WordWrap = True
        OnChange = EditDataChange
        FontSmoothing = fsmClearType
        ExplicitTop = 36
        ExplicitHeight = 481
      end
    end
  end
  object StatusBar1: TStatusBar
    Left = 0
    Top = 523
    Width = 1145
    Height = 25
    Panels = <
      item
        Width = 500
      end
      item
        Width = 50
      end>
  end
  object ProgressBar1: TProgressBar
    Left = 0
    Top = 517
    Width = 1145
    Height = 6
    Align = alBottom
    Position = 100
    TabOrder = 3
    Visible = False
  end
  object PopupMenu1: TPopupMenu
    Left = 136
    Top = 192
    object C3: TMenuItem
      Caption = #22797#21046#26032#22686'(&A)'
      OnClick = C3Click
    end
    object N7: TMenuItem
      Caption = #26032#24314#20070#28304'(&N)...'
      OnClick = N7Click
    end
    object E1: TMenuItem
      Caption = #32534#36753#20070#28304'(&E)...'
      OnClick = E1Click
    end
    object N5: TMenuItem
      Caption = '-'
    end
    object S2: TMenuItem
      Caption = #25490#24207' - '#20070#28304#21517#31216'(&S)'
      OnClick = S2Click
    end
    object G1: TMenuItem
      Caption = #25490#24207' - '#20998#32452'(&G)'
      OnClick = G1Click
    end
    object N9: TMenuItem
      Caption = '-'
    end
    object H2: TMenuItem
      Caption = #20998#32452#21517#31216#26367#25442'(&H)...'
      OnClick = H2Click
    end
    object N6: TMenuItem
      Caption = '-'
    end
    object D1: TMenuItem
      Caption = #21024#38500#36873#20013#39033'(&D)'
      OnClick = D1Click
    end
    object C1: TMenuItem
      Caption = #28165#31354'(&C)'
      OnClick = C1Click
    end
  end
  object SynJSONSyn1: TSynJSONSyn
    Options.AutoDetectEnabled = False
    Options.AutoDetectLineLimit = 0
    Options.Visible = False
    Left = 552
    Top = 352
  end
  object PopupMenu2: TPopupMenu
    OnPopup = PopupMenu2Popup
    Left = 632
    Top = 352
    object S1: TMenuItem
      Caption = #20445#23384#20462#25913'(&S)'
      ShortCut = 16467
      OnClick = S1Click
    end
    object T1: TMenuItem
      Caption = #27979#35797#20070#28304'(&T)'
      OnClick = T1Click
    end
    object N3: TMenuItem
      Caption = '-'
    end
    object R1: TMenuItem
      Caption = #25764#28040'(&R)'
      OnClick = R1Click
    end
    object Z1: TMenuItem
      Caption = #37325#20570'(&Z)'
      OnClick = Z1Click
    end
    object N1: TMenuItem
      Caption = '-'
    end
    object C2: TMenuItem
      Caption = #22797#21046'(&C)'
      OnClick = C2Click
    end
    object X1: TMenuItem
      Caption = #21098#20999'(&X)'
      OnClick = X1Click
    end
    object P1: TMenuItem
      Caption = #31896#36148'(&P)'
      OnClick = P1Click
    end
    object N2: TMenuItem
      Caption = '-'
    end
    object A1: TMenuItem
      Caption = #20840#36873'(&A)'
      OnClick = A1Click
    end
    object N4: TMenuItem
      Caption = '-'
    end
    object W1: TMenuItem
      Caption = #33258#21160#25442#34892'(&W)'
      OnClick = W1Click
    end
  end
  object Timer1: TTimer
    Interval = 100
    OnTimer = Timer1Timer
    Left = 464
    Top = 440
  end
  object MainMenu1: TMainMenu
    Left = 448
    Top = 136
    object F1: TMenuItem
      Caption = #25991#20214'(&F)'
      object O1: TMenuItem
        Caption = #25171#24320#20070#28304#25991#20214'(&O)...'
        OnClick = O1Click
      end
      object A2: TMenuItem
        Caption = #28155#21152#20070#28304#25991#20214'(&A)...'
        OnClick = A2Click
      end
      object N12: TMenuItem
        Caption = '-'
      end
      object N11: TMenuItem
        Caption = #26032#24314#20070#28304'(&N)...'
        OnClick = N7Click
      end
      object N10: TMenuItem
        Caption = '-'
      end
      object E2: TMenuItem
        Caption = #23548#20986#20070#28304#25991#20214'(&E)...'
        OnClick = E2Click
      end
    end
    object E3: TMenuItem
      Caption = #32534#36753'(&E)'
      object H3: TMenuItem
        Caption = #20998#32452#21517#31216#26367#25442'(&H)...'
        OnClick = H2Click
      end
    end
    object H1: TMenuItem
      Caption = #24110#21161'(&H)'
      object I1: TMenuItem
        Caption = #20851#20110'(&I)'
        OnClick = I1Click
      end
      object N8: TMenuItem
        Caption = '-'
      end
      object W2: TMenuItem
        Caption = #20316#32773#21338#23458'(&W)'
        OnClick = W2Click
      end
      object R2: TMenuItem
        Caption = #23567#35828#38405#35835#22120'(&R)'
        OnClick = R2Click
      end
    end
  end
  object SaveDialog1: TSaveDialog
    Filter = #20070#28304#25991#20214'(*.json)|*.json|'#25152#26377#25991#20214'(*.*)|*.*'
    Options = [ofHideReadOnly, ofPathMustExist, ofNoReadOnlyReturn, ofEnableSizing]
    Title = #23548#20986#20070#28304
    Left = 472
    Top = 352
  end
  object OpenDialog1: TOpenDialog
    Filter = #20070#28304#25991#20214'(*.json)|*.json|'#25152#26377#25991#20214'(*.*)|*.*'
    Options = [ofHideReadOnly, ofPathMustExist, ofFileMustExist, ofEnableSizing]
    Left = 544
    Top = 280
  end
end
