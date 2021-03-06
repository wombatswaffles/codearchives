; ID: 747
; Author: superqix
; Date: 2003-07-18 07:59:00
; Title: LaunchMenu
; Description: A Blitz-themed splash screen that let's you set resolutions and options

; ****************************************************
; * Blitz Launcher
; ****************************************************
; * Released under the LPGL by
; * Michael Wilson
; * All Rights Reserved
; * Inital source date 10/30/2002
; ****************************************************

AppTitle "Blitz Launcher"
Graphics 400,300,0,2
Global DisplayWidth = 800, DisplayHeight = 600, DisplayDepth = -1
Global AntiAliasing = 0, Wbuffering = 0, Dithering = 0, MultiTexturing = 1
ShowPointer

If Instr(CommandLine$,"/1280") Then DisplayWidth = 1280 : DisplayHeight = 1024
If Instr(CommandLine$,"/1024") Then DisplayWidth = 1024 : DisplayHeight = 768
If Instr(CommandLine$,"/800") Then DisplayWidth = 800 : DisplayHeight = 600
If Instr(CommandLine$,"/640") Then DisplayWidth = 640 : DisplayHeight = 480
If Instr(Upper$(CommandLine$),"/WINDOWED") Then DisplayDepth = -1
If Instr(CommandLine$,"/16") Then DisplayDepth = 16
If Instr(CommandLine$,"/32") Then DisplayDepth = 32
If Instr(Upper$(CommandLine$),"/AA") Then AntiAliasing = 1
If Instr(Upper$(CommandLine$),"/WBUF") Then Wbuffering = 1
If Instr(Upper$(CommandLine$),"/DITH") Then Dithering = 1
If Instr(Upper$(CommandLine$),"/HMTX") Then MultiTexturing = 1
If Instr(Upper$(CommandLine$),"/NOAA") Then AntiAliasing = 0
If Instr(Upper$(CommandLine$),"/NOWBUF") Then Wbuffering = 0
If Instr(Upper$(CommandLine$),"/NODITH") Then Dithering = 0
If Instr(Upper$(CommandLine$),"/NOHMTX") Then MultiTexturing = 0

Const MaxSizeX = 1024
Const MaxSizeY = 768
Const MaxButtons = 16

Global MenuImage 
Global MenuUp 
Global MenuOver
Global MenuDown 
Global MenuHot
Global MenuFirstX = 0
Global MenuFirstY = 0 
Global MenuNotHotColor = 0
Global MenuLastX = 0
Global MenuLastY = 0
Global MenuButtonCount = 0
Global LastCurrentColor = 0
Global LastDownColor = 0
Global LastDownColor2 = 0
Global LastDownColor3 = 0
Global LastDownColor4 = 0
Dim MenuSkipLineY(MaxSizeX)
Dim MenuSkipLineX(MaxSizeY)

; ****************************************************
; * Default Color Names
; ****************************************************

Global MenuBlack = RGBToInt (0, 0, 0) 
Global MenuWhite = RGBToInt (255, 255, 255) 
Global MenuAqua = RGBToInt (0, 255, 255) 
Global MenuBlue = RGBToInt (0, 0, 255) 
Global MenuFuchsia = RGBToInt (255, 0, 255) 
Global MenuGray = RGBToInt (128, 128, 128) 
Global MenuGreen = RGBToInt (0, 128, 0) 
Global MenuLime = RGBToInt (0, 255, 0) 
Global MenuMaroon = RGBToInt (128, 0, 0) 
Global MenuNavy = RGBToInt (0, 0, 128) 
Global MenuOlive = RGBToInt (128, 128, 0) 
Global MenuPurple = RGBToInt (128, 0, 128) 
Global MenuRed = RGBToInt (255, 0, 0) 
Global MenuSilver = RGBToInt (192, 192, 192) 
Global MenuTeal = RGBToInt (0, 128, 128) 
Global MenuYellow = RGBToInt (255, 255, 0)  
Global MenuOrange = RGBToInt (255, 128, 0)
Global MenuMagenta = RGBToInt (255, 0, 128)
Global MenuPink = RGBToInt (255, 128, 128)

; ****************************************************
; * LoadMenu()
; ****************************************************
; * Load bitmaps from disk
; ****************************************************

Function LoadMenu()
	MenuImage = LoadImage(LoadResource("launch_up.png"))
	MenuUp = LoadImage(LoadResource("launch_up.png"))
	MenuOver = LoadImage(LoadResource("launch_over.png"))
	MenuDown = LoadImage(LoadResource("launch_down.png"))
	MenuHot = LoadImage(LoadResource("launch_hot.png"))
	MenuFirstX = ImageWidth(MenuImage)
	MenuFirstY = ImageHeight(MenuImage)
	FreeResources()
End Function

; ****************************************************
; * FreeMenu()
; ****************************************************
; * Frees bitmaps from memory
; ****************************************************

Function FreeMenu()
	FreeImage MenuImage
	FreeImage MenuUp
	FreeImage MenuOver
	FreeImage MenuDown
	FreeImage MenuHot
	MenuFirstX = 0
	MenuFirstY = 0 
	MenuNotHotColor = 0
	MenuLastX = 0
	MenuLastY = 0
	LastCurrentColor = 0
	LastDownColor = 0
	LastDownColor2 = 0
	LastDownColor3 = 0
	LastDownColor4 = 0	
End Function

; ****************************************************
; * InitMenu(NotHotColor: inactive menu color)
; ****************************************************
; * This optimizes the menu system to skip rows and
; * columns that have no hotspot data
; ****************************************************

Function InitMenu(NotHotColor = 0)

	HotBuffer = ImageBuffer(MenuHot)
	If Not NotHotColor Then
		MenuNotHotColor = ReadPixel(0,0,HotBuffer)
	Else
		MenuNotHotColor = NotHotColor 
	EndIf
	
	LockBuffer HotBuffer
	For y = 1 To ImageHeight(MenuHot)
		Skip = True
		For x = 1 To ImageWidth(MenuHot)
			If  ReadPixelFast(x,y,HotBuffer) <> MenuNotHotColor Then
				If x < MenuFirstX Then MenuFirstX = x
				If x > MenuLastX Then MenuLastX = x
				Skip=False	
			EndIf
		Next
		MenuSkipLineY(y) = skip
	Next
	For x = 1 To ImageWidth(MenuHot)
		Skip = True
		For y = 1 To ImageHeight(MenuHot)
			If ReadPixelFast(x,y,HotBuffer) <> MenuNotHotColor Then
				If y < MenuFirstY Then MenuFirstY = y
				If y > MenuLastY Then MenuLastY = y
				Skip=False	
			EndIf
		Next
		MenuSkipLineX(x) = Skip
	Next	
	UnlockBuffer HotBuffer
	
End Function

; ****************************************************
; * DrawMenu%(DownColor,2,3,4: button down colors)
; ****************************************************
; * This function draws the menu, checks for mouse
; * overs and also draws "down" buttons
; ****************************************************

Function DrawMenu%(DownColor = 0, DownColor2 = 0, DownColor3 = 0, DownColor4 = 0, DownColor5 = 0, DownColor6 = 0)

	HotBuffer = ImageBuffer(MenuHot)
	CurrentColor = ReadPixel(MouseX(),MouseY(),HotBuffer)

	update = False
	If LastCurrentColor <> CurrentColor update=True
	If LastDownColor <> DownColor update=True
	If LastDownColor2 <> DownColor2 update=True
	If LastDownColor3 <> DownColor3 update=True
	If LastDownColor4 <> DownColor4 update=True
	If LastDownColor5 <> DownColor5 update=True
	If LastDownColor6 <> DownColor6 update=True	

	If Not update Then
		DrawBlock MenuImage,0,0
	Else

	; *** update mouse overs ***

	UpBuffer = ImageBuffer(MenuUp)
	OverBuffer = ImageBuffer(MenuOver)
	DownBuffer = ImageBuffer(MenuDown)

	; *** draw mouse overs ***
	
	DrawBlock MenuUp,0,0

	LockBuffer HotBuffer
	LockBuffer OverBuffer
	LockBuffer DownBuffer	
	LockBuffer BackBuffer()
	For y = MenuFirstY To MenuLastY
		If Not MenuSkipLineY(y) Then
			For x = MenuFirstX To MenuLastX
				If Not MenuSkipLineX(x) Then 
					pixel = ReadPixelFast(x,y,HotBuffer)
					If pixel <> MenuNotHotColor Then
						If pixel = DownColor CopyPixelFast(x,y,DownBuffer,x,y,BackBuffer())
						If pixel = DownColor2 CopyPixelFast(x,y,DownBuffer,x,y,BackBuffer())
						If pixel = DownColor3 CopyPixelFast(x,y,DownBuffer,x,y,BackBuffer())
						If pixel = DownColor4 CopyPixelFast(x,y,DownBuffer,x,y,BackBuffer())
						If pixel = DownColor5 CopyPixelFast(x,y,DownBuffer,x,y,BackBuffer())
						If pixel = DownColor6 CopyPixelFast(x,y,DownBuffer,x,y,BackBuffer())	
						If pixel = CurrentColor	Then CopyPixelFast(x,y,OverBuffer,x,y,BackBuffer())
					EndIf
				EndIf
			Next
		EndIf
	Next
	UnlockBuffer HotBuffer
	UnlockBuffer OverBuffer
	UnlockBuffer DownBuffer	
	UnlockBuffer BackBuffer()
	GrabImage MenuImage,0,0
	
	EndIf
		
	; *** save last state ***

	LastCurrentColor = CurrentColor
	LastDownColor = DownColor
	LastDownColor2 = DownColor2
	LastDownColor3 = DownColor3
	LastDownColor4 = DownColor4	
	LastDownColor5 = DownColor4	
	LastDownColor6 = DownColor4		

	Return CurrentColor

End Function

Function RGBToInt(r, g, b) 
	tempbuffer = GraphicsBuffer () 
	temp = CreateImage (1, 1) 
	SetBuffer ImageBuffer (temp) 
	Color r,g,b
	Plot 0,0
	GetColor 0,0 
	value = ColorBlue() Or (ColorGreen() Shl 8) Or (ColorRed() Shl 16) Or (255 Shl 24)
	SetBuffer tempbuffer 
	FreeImage temp
	Return value 
End Function 


LoadMenu()
InitMenu(0)

Active=True

Repeat

	Select DisplayWidth
		Case 640 : VideoColor = MenuFuchsia
		Case 800 : VideoColor = MenuGray
		Case 1024 :	VideoColor = MenuMaroon
		Case 1280 :	VideoColor = MenuRed
	End Select
	
	Select DisplayDepth
		Case 0 : DepthColor = MenuAqua
		Case 16 : DepthColor = MenuWhite
		Case 32	: DepthColor = MenuLime
		Case -1 : DepthColor = MenuPink
	End Select
	
	If AntiAliasing Then AntiAliasColor = MenuOrange Else AntiAliasColor = False
  If Wbuffering Then WbufferColor = MenuYellow Else WbufferColor = False
  If Dithering Then DitherColor = MenuTeal Else DitherColor = False
  If MultiTexturing Then HWMultiTexColor = MenuSilver Else HWMultiTexColor = False
	
	MenuOption = DrawMenu(VideoColor,DepthColor,AntiAliasColor,WbufferColor,DitherColor,HWMultiTexColor)

	If MouseHit(1) Then
		Select MenuOption
			Case MenuFuchsia : DisplayWidth = 640 : DisplayHeight = 480
			Case MenuGray : DisplayWidth = 800 : DisplayHeight = 600
			Case MenuMaroon : DisplayWidth = 1024 : DisplayHeight = 768
			Case MenuRed : DisplayWidth = 1280 : DisplayHeight = 1024
			Case MenuPink : If Windowed3D() Then DisplayDepth = -1 Else DisplayDepth = 0
			Case MenuAqua : DisplayDepth = 0 : Wbuffering = 0 : Dithering = 0
			Case MenuWhite : DisplayDepth = 16 : Wbuffering = 1 : Dithering = 1
			Case MenuLime : DisplayDepth = 32 : Wbuffering = 0 : Dithering = 0
			Case MenuBlue : Active = False
	    Case MenuOrange : AntiAliasing = 1 - AntiAliasing
      Case MenuYellow : Wbuffering = 1 - Wbuffering
      Case MenuTeal : Dithering = 1 - Dithering
      Case MenuSilver : MultiTexturing = 1 - MultiTexturing
		End Select 	
	EndIf

	Flip

Until Active=False

; ***************************
; *  Graphics Resources
; ***************************

Type TempResourceFile
  Field FileName$
End Type

Function FreeResources()
  For TempFiles.TempResourceFile = Each TempResourceFile
  DeleteFile TempFiles\FileName$
  Delete TempFiles
  Next
End Function

Function LoadResource$(Resource$,OriginalName=False)
  Select Upper$(Resource$)
    Case "LAUNCH_DOWN.PNG" Restore launch_down_png
    Case "LAUNCH_HOT.PNG" Restore launch_hot_png
    Case "LAUNCH_OVER.PNG" Restore launch_over_png
    Case "LAUNCH_UP.PNG" Restore launch_up_png
  Default RuntimeError("Resource not found.")
  End Select
  EncodeTable$ = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz{}"
  TempFile$ = Str Int Rnd(11111,99999)
    If OriginalName Then
      TempFile$ = SystemProperty ("tempdir")+Resource$
    Else
      TempFile$ = SystemProperty ("tempdir")+"~bb"+TempFile$+".tmp"
  EndIf
  OutFile = WriteFile(TempFile$)
   Repeat
     Read Output$
     If Output$ = "!EOF" Then Exit
     ByteCount% = Len(Output$)
     For i = 1 To ByteCount% Step 4
       Word% = ((Instr(EncodeTable$,Mid$(Output$,i,1)) - 1) And 63) Shl 18
       Word = Word + (((Instr(EncodeTable$,Mid$(Output$,i+1,1)) - 1) And 63) Shl 12)
       Word = Word + (((Instr(EncodeTable$,Mid$(Output$,i+2,1)) - 1) And 63) Shl 6)
       Word = Word + ((Instr(EncodeTable$,Mid$(Output$,i+3,1)) - 1) And 63)
       Byte% = Word Shr 16 And 255
       Byte2% = Word Shr 8 And 255
       Byte3% = Word And 255
       WriteByte OutFile, Byte
       WriteByte OutFile, Byte2
       WriteByte OutFile, Byte3
     Next
   Forever
  CloseFile OutFile
  TempFiles.TempResourceFile = New TempResourceFile
  TempFiles\FileName$ = TempFile$
  Return TempFile$
End Function


.launch_down_png
Data "YL1EHmqA6We0000DIKX4KW000P00004i20C0003QQHNm000016T1JK400B6EVFjHam00021ZI59D001w9G00W8C0"
Data "0Fd}0020w000TJ000EfW000wbm005s{NgPdK0003051CL4K0003}}}zcPlzhQ}jlR}PmSFPqTF5rTV5wUkrvUUn{"
Data "VkY3W{E8YD{8YDwDZTgIajQIajMNbz6Nbz2SdCoWeCUXeSYcfiEbfSAhgxwgghsmiBglhxcqjRIrjRIrjBIrjRMq"
Data "jBG0000000000000000000000000000000000000000000000000000000000000000000000000000000000000"
Data "0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"
Data "0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"
Data "0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"
Data "0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"
Data "0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"
Data "0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"
Data "0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"
Data "0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"
Data "0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"
Data "0000000000000034tmw}000Qxqb4GLHud69W60M32W04q2WQP00WW4RH80C00JIA1Xa02A1HDCW0G02Dea46009e"
Data "50qo0110esYG0O006aM33004q2WQP00WW4RH80C00JIA1Xa02A1HDCW0G02Dea46009e50qo0110esYG0O006aM3"
Data "3004q2WQP00WW4RH80C00JIA1Xa02A1HDCW0G02Dea46009e50qo0110esYG0O006aM33004q2WQP00WW4RH80C0"
Data "0JIA1Xa02A1HDCW0G02Dea46009e50qo0110esYG0O006aM33004q2WQP00WW4RH80C00JIA1Xa02A1HDCW0G02D"
Data "ea46009e50qo0110esYG0O006aM33004q2WQP00WW4RH80C00JIA1Xa02A1HDCW0G02Dea46009e50qo0110esYG"
Data "0O006aM33004q2WQP00WW4RH80C00JIA1Xa02A1HDCW0G02Dea46009e50qo0110esYG0O006aM33004q2WQP00W"
Data "W4RH80C00JIA1Xa02A1HDCW0G02Dea46009e50qo0110esYG0O006aM33004q2WQP00WW4RH80C00JIA1Xa02A1H"
Data "DCW0G02Dea46009e50qo0110esYG0O006aM33004q2WQP00WW4RH80C00JIA1Xa02A1HDCW0G02Dea46009e50qo"
Data "0110esYG0O006aM33004q2WQP00WW4RH80C00JIA1Xa02A1HDCW0G02Dea46009e50qo0110esYG0O006aM33004"
Data "q2WQP00WW4RH80C00JIA1Xa02A1HDCW0G02Dea46009e50qo0110esYG0O006aM33004q2WQP00WW4RH80C00JIA"
Data "1Xa02A1HDCW0G02Dea46009e50qo0110esYG0O006aM33004q2WQP00WW4RH80C00JIA1Xa02A1HDCW0G02Dea46"
Data "009e50qo0110esYG0O006aM33004q2WQP00WW4RH80C00JIA1Xa02A1HDCW0G02Dea46009e50qo0110esYG0O00"
Data "6aM33004q2WQP00WW4RH80C00JIA1Xa02A1HDCW0G02Dea46009e50qo0110esYG0O006aM33004q2WQP00WW4RH"
Data "80C00JIA1Xa02A1HDCW0G02Dea46009e50qo0110esYG0O006aM33004q2WQP00WW4RH80C00JIA1Xa02A1HDCW0"
Data "G02Dea46009e50qo0110esYG0O006aM33004q2WQP00WW4RH80C00JIA1Xa02A1HDCW0G02Dea46009e50qo0110"
Data "esYG0O006aM33004q2WQP00WW4RH80C00JIA1Xa02A1HDCW0G02Dea46009e50qo0110esYG0O006aM33004q2WQ"
Data "P00WW4RH80C00JIA1Xa02A1HDCW0G02Dea46009e50qo0110esYG0O006aM33004q2WQP00WW4RH80C00JIA1Xa0"
Data "2A1HDCW0G02Dea46009e50qo0110Z3GmKu52}G{eOmooMRGn48UHVvc0nB}}B0p}69WO}WD368J}Ce14yGSto6o0"
Data "06AXXYD70GfWXX6mA6242H8100A8YKQE6WLa0e00j5FH3i8m23oTs91L}}yxPnU6h6f6qSseZpuTXUjnaF2}2z6H"
Data "JXv8HGPCRfosTLCaMl8xfvNhCVzG4lMVDi9HPS}qrkZWxs5cXcEVliaywTO5hT7B7BuAMg6H7FAghfBuGolyrMKG"
Data "s7HllWQSFIsnW9ScRSV0fVOKZMar}120Lh9ROH26mUXtCM4}rJa5bRt}iuqvfwinJaMnYnte16}Ncq899ydv{hT0"
Data "gXTGnbF7V9J5{cm0gU2de2O43ZH9MNX0NijBl0Ngqn5q2j7V3UZYedaQw609ZmiX90wo1hoFWDi0NZLQ7NvtnffD"
Data "Ex1GyMWjNNmLM9JNR2yrqwKCk{iMaNYO5h5pbcSFbJOeVHUa0PtCFuVQ5agyMn6{P{UiZk7Nb5e2{GX0InNjC0Z2"
Data "m4h8m6WCxF{}SK50WKde2XhT4bztBttWUjmrJVzsicRePN7rlz8IPgwx9NqHziYUwQ6ij8UT4idU2OKoQ4QT1gNg"
Data "vWqizXCirxi1sQC37vZcqTFYSyLykbEgU4TirIQYBO7cn4dDdNuEF4P2lVrDtKAAuYcok}MN3nz53cWsM45D662R"
Data "OQg{JbhSgyj2rnYlB4Ti0ma}SXy1QAcY7OH165ZC4f7HoZ2uj}t}1{A0c30B8mkQlNeFVIftq3lwBqDy7joaRuVU"
Data "2eJZvli6KQl2XrYvIH71kI5yKdEDC97CWJUTO{{EbuPyUJeM2g0HeJ0XwNEc0hliVolMDWy9q5pXNM}OGLlBgPUD"
Data "NN40Haq}eskObptU2IREUqf9aXaYxozfNEVot8JB8DJsOicx5KKunIyFFWBGMaKh38Cmy1Ip8QDG{l}VE0RTSAbM"
Data "Tx6Kxc7Rs}H93EUTcal{PLaBpX0p3dr6qr6O}YUKF6EGMwLx9o}z6wyavHZk454}OrNRq0PXgv8zWH4qf{H7GrDR"
Data "JnOKEBKZXMz8nz0UbcaMvlEy4YPUief1h2jw4Kfy42LfJ1k9zNVJEZQp9}7RsJ8J7g7SMvR1k1vY8cKikEmLzU7Y"
Data "MxxZ9G2jrRA3C0W4bvGA1EB}}sDZqZOW8gGukwqc9khDFN00igzPPlWN8CW{YaOcPL5eFRP73FXfXOpNV6Oymfpd"
Data "Q}V4tOR53xhkoYKavGyIQd8FdT6fwtiquqQ31E9p1p{TbPY{KjRJze2DMDXvU2WR}fYQQTEWKOQWtf0cO1RlFYvC"
Data "mhyy1cOzPSYKYPf30s13GnkKNSkhBdaoUi{oo9hi6yuF0MYjbXK0OHZcn3d4ry3}}q3ne0XJ5BMcTJi8wiqUHvVI"
Data "hCtobsGT{0wx5kymIJrFq0h}uR3{cZL0JXe5qUV{NwUlohQ1Qj07rbBcK6BNkI2XSIxqA2RVOI8}eYF56QJcOXkw"
Data "6KmI5WJeJj2DfO7oDniOGgvODS{93Jjw58RwHPLFwOxJHxgZdWA8TlqGHcx6d{{1LJgoVV{1AV8}s7bVe2tNFw0w"
Data "zZTM0}22VmoyJC3s4W{GmSZ3XBqvG1m0rRv0zp22Ou61Z{NVFoOmymiek8312Z7zDzXE}E3BTv19}r1K}W4A}M3W"
Data "PiMY}ESN1YvW9BAWvZk00A9TX0W8ymATWceV8oW9WfptvHz4{ES71awMFn}mcVFsEoCFmmT8xVW7Bih2m0eo2SJa"
Data "5k8dN4HX0n1Dd}{pyp7y}iO55UHZW6IGdtyWoHfY{kUl33oCtx6siX3W4wZv{llDVqxsVwzXOgoWGX5x1la8R2W0"
Data "SoPQjWC8G8kvgm08mr0qTj31GItu}z}NGOVIb}LHarWKHCJ5gNIv2RcvXz8V4y9829l2}Heo7KfUjPNOPLK4xMsg"
Data "{XxyMqGiTBBEmzl8YQnExl2z9DaRixv}qHbJj8JpIJc8cbMNeom61ESfw3GEtrqDl1ioCdj34gqi5Y}EHcP8JzdI"
Data "WP9emM1XTxM}2q2BrRK02CB0Crf14DJ}}vTI3zBITXEZXw2dz4sEkG{tdVih8CnHzrrEsbfneNgHFs{G7nReUHXG"
Data "0bAeNdWfqXjFXH8y9k8ASr9d6KzXeVRaVdHii4tZXY}zEeUD537xQz6KKHH6tB1N5bnpEDCK8TkIHuA{BDDd1Tq}"
Data "YGn8FhiwZ515nOE5kuLaWbOf5saNv}C6N0BGOZuj2CCm56}BPXo47GhYpU}}wSQenJPhtNpfH0}2RkRSF{bB2B}N"
Data "Vr4M29GpddrhLleAzWXZHhE5wYY5Y}VgbJq}eAribcek{z3{2IIT1e7WTXCg{iaWuWHg1V3N6iOUTKOX{auM27Os"
Data "bCkmcAE}anQdjgn3MvCIALoWcI9v7M16xbuD2BVRdusTB9gy79yw6zvBc1ktLPtNgzCyEIRvR7LhPCtppGWgn1Jv"
Data "sual0MYneXq6GHY89WR1i8Vz}mzEimMSX3Q2y{fSVDZYsnvfhgN95RZZNuIO40ihUwmv444KGiBeTFHdAGsisiOy"
Data "Kqy}vrfhlckuFYZFePPxtIHV5aJnWh8VByw}Dz8nPW9FBON4vm9rwsU3YUEh7cBCz}0imesZQ}O3udpg1GRPotHQ"
Data "5TLUgDHrKxxDyZiJya3879VwMZ227voTVEJAe7qby2{ELm5eiQ8TX44O20RJ4RlAoEB}VwEI3Dm4Zp6p1uq{{KgQ"
Data "AoIzuwv}awo1edRxIabiGTGcN4}8xXJcmKp5EaLd39Hrdv3ygS16akfDNlG8is3IWg00Wf0SybLTkgYGefdHm75j"
Data "b9JxoP1{BQl19OOGxgzZ48HXL6zGh3PC6xh7bwvPlW0{wW8iX9ayfMob6lgfT7xTD4fJwVOSoacFntahVyDv2Y0Q"
Data "Jb1zHqrGt}zYRyP}Pm4NfT2nK11uW68CCDd{uKGnuGipciam4y23hh31LnIpK0raG5PE241DXtLQ7w18AZ1W0JyX"
Data "PTlldwnOqmMkW82O3H10D9oWuiJBHHTdnvseMLbHJK3t3ve918ihWXPYCnrRWmENoH2A5KTyutSVG02DJb0DCW0G"
Data "GACHCiW0G02DHiWW0m01D8e66G08e54qo01008sYGGO00cWK3J80442ZQ901W00QHOCC00JGA1fa0220Hj4W0m01"
Data "D8e66G08e54qo0100D5Y{9qKe42spWUKc07NJ8bUwcmuGJCM8816nx866G08eD486MG08806mmugy6OCj5d0tzz}"
Data "y6BCvE3PGmBIyXDYni{lB8YvLlXc4VoxHZx}XUtPGDh4yWwogXVLG3Jm}IyhEunW}uUXaC1U5LHxm00WW0P3X40s"
Data "Op098qV0otz8AoHWu1Te6XMqM0MB8Jy}}O1i}01lJ87lo}Z{auqTmNW4MWM32S1RJq2RH5x{X6uM0OClq83w}OMT"
Data "3xGq1Dkcayy}UDWPtlu34c}{2p605A80kFNum1Va2048eC5HP36oipFyUuyqu}}z7wCGaLEiKF3xEsJZ1tXZod}C"
Data "9MqixBXt{Rt}py790Dg2yfDTWE4hnZfXPdRSoHQqWfxX7yCVXj}}6TZnAIGM00JGOCWX30niuWpVt}nvXzX1zPq1"
Data "N{643V3mW3OxWNQSC8aolCESjnR6446odb4Kc3c{i}zYuEFyykSxkcPEFBFWR0oW1MN02Fd9mCZAIkHqFZu0481v"
Data "C{X148QXy5EoZCOC}}{VR71Ai5G9RGE9971trrxUskszozV}68WTklSY6VhyjWNLdXtVieoIh1jXn9iRisASJZVM"
Data "d54ZO5UlxxOBD}UzApwA0yUFBhYYs4TfFZyjCJLTZWmnHSe7UP0mQz2dayqcmePH1Y5AqBg24I{dxYYACcnNCLNE"
Data "cykZsGjQ163U35OO168WEXUxU2XKgVl}l{UXHKji4aIjixfG4Nfkxe70C9FBlByH16M7CRpw1QfH7UuNLfPzY245"
Data "SKvZeNr{288CfkHHXLdaFdTFAR{F64AoWd}t4vyEhQxy8Tj5Pq5Og9m8vaohDkdP26protmZ2}bcJ2RdshrioM88"
Data "mjYXriSCawOWY7Wig7u4ODuALXY4OUXp55jHYWRw}t{tuoPLfD3MBd4ofiZEoos7X92Ny79u{GyE4UDnIN4i0rLX"
Data "iWqQqWkCK{KaR2S5{mkFUFuN2Ns7iFcQu{WI3tz}x9mrulRLzJoONM7zhkWYj4PAV7{i4P}PpbPUh{QYo4YpFLed"
Data "hAOv5UyP1FhdsZcLlCXdNJxaUmcWmPD3640BindOUO6PueVeTmPc7WPXBaw6Rt}GLZZpVFt3WhzwOUJ{{lCpvs}m"
Data "1YqWvkS39bcmfrbPt{5ONGMtvFTd1dv8EnoSouJPMNv0f3Z1Bi82M7x{}Sd20Z81lWnBa0UOtu0rFGyFpvD}3C0I"
Data "ZO{LwSSF1jQVlnWWa1Ke9iJmv}rtOExWPsTwYMmUG00DgWXX0Rhs9J0DmLRlixpxHd2p82OGu6Nvy0EqonaI8KoW"
Data "dMM}y6iHultwwHqe2Zxz1zPWu7Ouk0dDJdZL7JEm16NXUVSLQG4SBEAOmClcGRKzk1W4rZ7V}pFz0nP{RC34yfBX"
Data "BzYVx6XBk042QF14o7V8KHIWiWlgg9{l}x6Jl1M74RenXPcaLZEhmBS}FpaPldnb16qf0Erj83PWMC3RJPd{uT40"
Data "RM}plFlpdO7ho}S}33pWZXSprdOuG00DgWXX}mzg0CF1bty2VEzW4V8NgoPC00eWK5oWjbNnxXS1T}7P}}na05Qt"
Data "u3qRV8YAXk3cHHxGuda6jXyCk1OACyDhRVQVtnY4ldm3xKzcO83vypUQFe006ZIL{eSl39oiB0o}VZAyUlmE8lGD"
Data "s4w4x1x}m}0MxsOP901iwm0hJwH1xE{WGppm7}toxirlKC73yFaV1{hcioyCI7NrV{oQcQ1v6jqAqCuXqA9tJf0J"
Data "FaEK0NCyEoYzi3C2ZVxmw1LGz2i3weO{W00Q73darqkWondv63ZPVxvc{WChZPd}VlW9AcPPVtxwZ0WD9ePFl}0C"
Data "474mWlQBS2C4G1iycA0cClv}ZQNPoyeEh710uOIwMGHOix}}2cy5im3J1ZksQfqDjEs55JCJSlpu}WooBUpB6}OV"
Data "e8g53Rmr4cmYznUGqMmCxDy}VKDDQW01c3cZ5G1168fkOPHCoYJw}v{C4bQsLLXW7z1U19}cbS6z3sV}c13njcWd"
Data "tG1WTgxyBPzJwUKaXFHi0x787rX5hdhKrsTZlC4GKyRsILD9MS589WrEPc1vmIAYN1{XlJ{mgzByQGtC9NI1LtK4"
Data "R9N03vHM86d1uGd6eIPNXP7bpZV0thmnv4C03Re9gf}W}RcGtIA}OLjKlw1ivVW2FaRe0KupO3iwuEeH2His8WpN"
Data "3DNx4yTsDTZM50O6n0uMt1QZ0RYXs7QAmFm6TopKM8006Xn55XA0k1ooUGE{mmArg20qoeNUL4LIZslF1guY45az"
Data "iHjAC0t5PYlwfXSO00YWmL5aZG8u00YWqGWPP00WW0PTaJMKm0CQc0aGGADea06001f5Wmm01D0e6cG08816qI03"
Data "004qYWOP00YWKJJ80400ZQ911W02Q1GDCW0GGADea06001f5Wmm01D0e6cG08816qI03004qq1DKD04AECGV4AC8"
Data "ZpPwQ048eD7Xzq46009e60{}}}x9WFsCFJW0pmN3YE{OUsk{V}k3xKeJyAegyAGoOYiFlhr4a0jR81FF105000tZ"
Data "2FdwYO5J5A{AJty4M1dUCGYo}dx788wvj{RBE{pQ8CAWIraGMtb{ORj70AuUj4JZ7VZQ5sHXh5k0009e64V8JuAN"
Data "9B3{{GDAxhz1opsnvAMVE7U6i33}}lU1WG}lLXuo0K003Uy8{OUn{GO5C4FM4Vy1GcnB6}ymSE98zdmy3A{}Vs3b"
Data "nBULXqm04433Dq8{Cp0o}m5VGl7zsryMNcpbDnl3Bz2Ig5{mzOdWhJtGZJKiVA2jGVzmxlOK{F7}EoTYAm}8mX{m"
Data "iwrnWaz}kB}}PE7x{Hse4gBlA}Y26wJw1o20Xc{4}69WXwnw1zqxnl0RM{tCpl3}zny6zf{G0zx1Mtk{YuFh1H01"
Data "sXg4Eq9OsN}yPa1ivG5MMV{1cL86lvk{}mHjvla3pBi}P27w}dw7N7031m01D7oRlTyPEDX1vx03Qma{8Q3ViIW1"
Data "NGFmYmdOnle9NdZ}0rXF}qIiR0Hj3S9pbZKiASEsye0EwFw7OstsjvSlePj0G5k0Vd8BClp}0j77n}3b4yhr8W01"
Data "D6mZvES}y59SO3AN4HNWOS5{QGmRm}V}R2oW9UwWCeENJu05QOCP3pC32n5r12Sp0pCFC4GvHFdOSQtH}}SJbY3u"
Data "URaOs8L15p90zB5pCln1kLu48826RP45382lNvd{WHPiCht{WMFbEWimd5YOO5K8Q0dxdzy4Rxg00lImnnUIeCeV"
Data "qbZ6o01ytr7ltm48e64R8JyW7RYVu9C4E7zYtud52gezUDxz8Nfx3XBuHyAjCQ3TS3WwDR}HeXOWW8PlaGLii}Bm"
Data "C00hqCzCeg8u{iWWtxE1xxU32q5KuiXHIE3xJyAtnX03lZ8WNPq01001D5mZvCj}1b4X8I4cO2tzXvCJQvNE0Aav"
Data "s46n0GxRRw3H5XPG{{mdtbknGE3dUw9F}SSDWD7{{ISZ1yerc001D5oBh5{GwthOGNV7}Fpm1PSobZ{GxOJWVF7Z"
Data "vIy69ixVZF{V4Dfg{ka3K0NiaX8c{1s0f04MXc{CR1yP{Db}85t0mm0GGCCrXq2lw{5W{C7E}USJ4xRBb4201H8R"
Data "a84JmL{WhJsiN0p}S9}{0m5}}Z6m2CBKS3FznxU322VW0{hxXdR13GC3G02DXFcGdz0x{WZFXq16267gOJV3FC2d"
Data "1LaX0X3K0jM7D1mDrG8GGCEro486n6{quK5HJtGTGMvbWaqVG00Dro9ho0A001f5Wmm01D0e6cG08816qI03004q"
Data "YWOP00YWKJJ80400ZQ911W02Q1GDCW0GGADea06001f5Wmm01D0e6cG08816qI03004qB4Tx1nB8awln8OG22A3H"
Data "mSL11W02Q2GClzCTVFzFx9ced}voeQa42A3H2A41{FIBXyW8{VuBNI5008qMMOCC00JGQ8GCCW0GGAD55at1bnyC"
Data "73mCFxynyp5y{ydE1MImVli7tkJmwIShPCNYXzzCV4WpyW01D1eXj0GlVm6hYJy2Vx{myJ7y}CB0zVSByx}}33zO"
Data "sRzy05A}GVkxtlrdOFWlWj02442ZHHODmVTVZ28yu1NuSF2NXHzqVF{N}nnYZE1BDFxpSg6e00YWqHn2Gy0fyO{T"
Data "2NND5hCmwzyl}nb{C}2m2u3lyk4LOFY6l70L88168uIMWFNRwn{e8ipWwk8xQ0aGPCqH{W9XW00QZH1QWhVV67cm"
Data "BMF5ivOR88168uI6uESt3W5MR142laa1{HXp1008eD5AdORW2mCVAsJPxb{6xr{H9DX1I{s}l7cFHGz008tc49g0"
Data "xw3Tffoi35{{WUA1cU7lCzGDLvpVtttzWUKkKmO6W00QpI4q0Nz}0S5VJkPlNpWPmVTP}ML52Nr{JeOVpBpO1hm0"
Data "0cXqFeJA07K{17OP1UPr6Bz}e{KFw7m8G02D5baq1R1g6yiLeJXssG44q6YHDSW0G02DHiWW0m01D8e66G08e54q"
Data "o01008sYGGO00cWK3J80442ZQ901W00QHOCC00JGA1fa0220Hj4W0m01D8e66G08C00VB7oO8GC3FW000019HKv4"
Data "ha9WWW00"
Data "!EOF"

.launch_hot_png
Data "YL1EHmqA6We0000DIKX4KW000P00004i20C0003QQHNm000016T1JK400B6EVFjHam00021ZI59D001w9G00W8C0"
Data "0Fd}0020w000TJ000EfW000wbm005s{NgPdK0003051CL4K0003}}}}}0Fy00Fy0}}y0W800}m3}}m3}W03}0020"
Data "003}W830mC20W800000000000000000000000000000000000000000000000000000000000000000000000000"
Data "0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"
Data "0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"
Data "0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"
Data "0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"
Data "0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"
Data "0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"
Data "0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"
Data "0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"
Data "0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"
Data "0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"
Data "000000000000003B4Rn}0008Aab4GLHud69W60M32W04q2WQP00WW4RH80C00JIA1Xa02A1HDCW0G02Dea46009e"
Data "50qo0110esYG0O006aM33004q2WQP00WW4RH80C00JIA1Xa02A1HDCW0G02Dea46009e50qo0110esYG0O006aM3"
Data "3004q2WQP00WW4RH80C00JIA1Xa02A1HDCW0G02Dea46009e50qo0110esYG0O006aM33004q2WQP00WW4RH80C0"
Data "0JIA1Xa02A1HDCW0G02Dea46009e50qo0110esYG0O006aM33004q2WQP00WW4RH80C00JIA1Xa02A1HDCW0G02D"
Data "ea46009e50qo0110esYG0O006aM33004q2WQP00WW4RH80C00JIA1Xa02A1HDCW0G02Dea46009e50qo0110esYG"
Data "0O006aM33004q2WQP00WW4RH80C00JIA1Xa02A1HDCW0G02Dea46009e50qo0110esYG0O006aM33004q2WQP00W"
Data "W4RH80C00JIA1Xa02A1HDCW0G02Dea46009e50qo0110esYG0O006aM33004q2WQP00WW4RH80C00JIA1Xa02A1H"
Data "DCW0G02Dea46009e50qo0110esYG0O006aM33004q2WQP00WW4RH80C00JIA1Xa02A1HDCW0G02Dea46009e50qo"
Data "0110esYG0O006aM33004q2WQP00WW4RH80C00JIA1Xa02A1HDCW0G02Dea46009e50qo0110esYG0O006aM33004"
Data "q2WQP00WW4RH80C00JIA1Xa02A1HDCW0G02Dea46009e50qo0110esYG0O006aM33004q2WQP00WW4RH80C00JIA"
Data "1Xa02A1HDCW0G02Dea46009e50qo0110esYG0O006aM33004q2WQP00WW4RH80C00JIA1Xa02A1HDCW0G02Dea46"
Data "009e50qo0110esYG0O006aM33004q2WQP00WW4RH80C00JIA1Xa02A1HDCW0G02Dea46009e50qo0110esYG0O00"
Data "6aM33004q2WQP00WW4RH80C00JIA1Xa02A1HDCW0G02Dea46009e50qo0110esYG0O006aM33004q2WQP00WW4RH"
Data "80C00JIA1Xa02A1HDCW0G02Dea46009e50qo0110esYG0O006aM33004q2WQP00WW4RH80C00JIA1Xa02A1HDCW0"
Data "G02Dea46009e50qo0110esYG0O006aM33004q2WQP00WW4RH80C00JIA1Xa02A1HDCW0G02Dea46009e50qo0110"
Data "esYG0O006aM33004q2WQP00WW4RH80C00JIA1Xa02A1HDCW0G02Dea46009e50qo0110esYG0O006aM33004q2WQ"
Data "P00WW4RH80C00JIA1Xa02A1HDCW0G02Dea46009e50qo0110esYG0O006aM33004q2WQP00WW4RH80C00JIA1Xa0"
Data "2A1HDCW0G02Dea46009e50qo0110Z3Gmaub2}VyW52}53W62pqXiBceO{0s5nqaD8{7WEn03112bWJSAg0m00cWq"
Data "GWOP00YWqGWPP00WW4OZP901W00QZP111W02Q3H21Xa02A3H21ba0220HYDaa06001gDa446009eD4866G08eD48"
Data "6MG088168sIG0O006esGGGO00cWqGWOP00YWqGWPP00WW4OZP901W00QZP111W02Q3H21Xa02A3H21ba0220HYDa"
Data "a06001gDa446009e50qo0110esYG0O006aM33004q2WQP00WW4RH80C00JIA1Xa02A1HDCW0G02Dea46009e50qo"
Data "0110esYG0O00eiN{45802zaw}y1PPFZXFup1HhhUNt0MD{cQSOEl40eWW4R7iWOP00YWqGWPP00WW4OZP901W00Q"
Data "ZP111W02Q3H21Xa02A3H21ba0220HYDaa06001gDa446009eD4866G08eD486MG088168sIG0O006esGGGO00cWq"
Data "GWOP00YWqGWPP00WW4OZP901W00QZP111W02Q3H21Xa02A3H21ba0220HYDaa06001gDa44600A8}4K6em2sBe6g"
Data "0220Hj4W0m01D8e66G08e54qo01008sYGGO00cWK3J80442ZQ901W00QHOCC00JGA1fa0220Hj4W0m01D8e66G08"
Data "e54qo01008sYGGO00cYWTr3H173W4F{1p64dshYVC0OhqLf{mnWyH6lv0g400cXq{7sG0O006esGGGO00cWqGWOP"
Data "00YWqGWPP00WW4OZP901W00QZP111W02Q3H21Xa02A3H21ba0220HYDaa06001gDa446009eD4866G08eD486MG0"
Data "88168sIG0O006esGGGO00cWqGWOP00YWqGWPP00WW4OZP901W00QZP111W02Q1GDCW0GGADea06001f5Wmm01D0e"
Data "6cG08816qI03004qYWOP00YWKJJ80400ZQ911W02Q1GDCW0GGADea06001f5Wmm01D2mNFq{a82PN8r}8HH008qE"
Data "BWuo0110en4oo01008r6o203004q6Y633004q6Y43380442Z4JB80400ZKR880C00JGQ8OCC00JGQ8GCCW0GGACH"
Data "CiW0G02DHiWW0m01D1eXWmm01D1eX0mo0110en4oo01008r6o203004q6Y633004q6Y43380442Z4JB80400ZKR8"
Data "80C00JIA1Xa02A1HDCW0G02Dea46009e50qo0110esYG0O006aM33004q2WQP00WW4RH80C00JIA1Xa02300SmOE"
Data "chj71Z400000IKLEHAv2O880"
Data "!EOF"

.launch_over_png
Data "YL1EHmqA6We0000DIKX4KW000P00004i20C0003QQHNm000016T1JK400B6EVFjHam00021ZI59D001w9G00W8C0"
Data "0Fd}0020w000TJ000EfW000wbm005s{NgPdK0003051CL4K0003}}}{qjRIrjRIrjBJx{}lszlRnyV7jxUtixEpe"
Data "wEZZu{FVtz}UtjxQsjhMrjRLrTNHqT7GqD3CpCp8oCZ7nyV3myF2miA{lhwzlRswkhgvkRcrjRMqjBG000000000"
Data "0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"
Data "0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"
Data "0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"
Data "0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"
Data "0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"
Data "0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"
Data "0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"
Data "0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"
Data "0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"
Data "0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"
Data "000000000000000TgVTS000Qa4b4GLHud69W60M32W04q2WQP00WW4RH80C00JIA1Xa02A1HDCW0G02Dea46009e"
Data "50qo0110esYG0O006aM33004q2WQP00WW4RH80C00JIA1Xa02A1HDCW0G02Dea46009e50qo0110esYG0O006aM3"
Data "3004q2WQP00WW4RH80C00JIA1Xa02A1HDCW0G02Dea46009e50qo0110esYG0O006aM33004q2WQP00WW4RH80C0"
Data "0JIA1Xa02A1HDCW0G02Dea46009e50qo0110esYG0O006aM33004q2WQP00WW4RH80C00JIA1Xa02A1HDCW0G02D"
Data "ea46009e50qo0110esYG0O006aM33004q2WQP00WW4RH80C00JIA1Xa02A1HDCW0G02Dea46009e50qo0110esYG"
Data "0O006aM33004q2WQP00WW4RH80C00JIA1Xa02A1HDCW0G02Dea46009e50qo0110esYG0O006aM33004q2WQP00W"
Data "W4RH80C00JIA1Xa02A1HDCW0G02Dea46009e50qo0110esYG0O006aM33004q2WQP00WW4RH80C00JIA1Xa02A1H"
Data "DCW0G02Dea46009e50qo0110esYG0O006aM33004q2WQP00WW4RH80C00JIA1Xa02A1HDCW0G02Dea46009e50qo"
Data "0110esYG0O006aM33004q2WQP00WW4RH80C00JIA1Xa02A1HDCW0G02Dea46009e50qo0110esYG0O006aM33004"
Data "q2WQP00WW4RH80C00JIA1Xa02A1HDCW0G02Dea46009e50qo0110esYG0O006aM33004q2WQP00WW4RH80C00JIA"
Data "1Xa02A1HDCW0G02Dea46009e50qo0110esYG0O006aM33004q2WQP00WW4RH80C00JIA1Xa02A1HDCW0G02Dea46"
Data "009e50qo0110esYG0O006aM33004q2WQP00WW4RH80C00JIA1Xa02A1HDCW0G02Dea46009e50qo0110esYG0O00"
Data "6aM33004q2WQP00WW4RH80C00JIA1Xa02A1HDCW0G02Dea46009e50qo0110esYG0O006aM33004q2WQP00WW4RH"
Data "80C00JIA1Xa02A1HDCW0G02Dea46009e50qo0110esYG0O006aM33004q2WQP00WW4RH80C00JIA1Xa02A1HDCW0"
Data "G02Dea46009e50qo0110esYG0O006aM33004q2WQP00WW4RH80C00JIA1Xa02A1HDCW0G02Dea46009e50qo0110"
Data "esYG0O006aM33004q2WQP00WW4RH80C00JIA1Xa02A1HDCW0G02Dea46009e50qo0110esYG0O006aM33004q2WQ"
Data "P00WW4RH80C00JIA1Xa02A1HDCW0G02Dea46009e50qo0110esYG0O006aM33004q2WQP00WW4RH80C00JIA1Xa0"
Data "2A1HDCW0G02Dea46009e50qo0110Z3GmKvP2}O{fOmooMRGna4f6efWD441CL3Ln550C009eD4866G08G2iLBJ48"
Data "mh38CGJv}q{j8DPkP{bDTpjzyocb3Md2uHzJPdfDMZH11AH8{bac3S7j}K5fTHdZXL9RzAOG5BcuTFv7J5xFn2b0"
Data "CT{bOdP6w}Iwcg{AFkWaXNYeSnd3dvRvapq8fBwxBqBMTh47{6hwSmpCMtQss1{6FmBGIZOx2C9049vu80YYeC29"
Data "ztyuh61P5vhIDBOruIVXwbvwcNmpEzk}7MJi0AgyOp{ZWHKQChjj12nHnY3fP4M4zmSKbqSW{vmXBpbiQo3l2NHd"
Data "q9VhyPHmXFg4RWIT7q2hGJkZtIYUJV0H2kIeU4s1xleIM0CmXQTPw4w6g3uYCck}ICX9MOfnCA1h0kw1eLv{3gi9"
Data "FbOeeaX3JexpNrERl5y1Q2c37GP160ptW0wCXa3s}gyu5QGC6hDsciqbNDS37FZpyRK9}EtB2Z2OAcsb{lOflLT8"
Data "bq0wriwFDG55vNJUMY2ie{yYmKp6gR33IeE5{3rVm0mK83EfnomNkIvGYoJrF1sspB78Cy6UQU8Ihx7Rn0NgfA}y"
Data "FlHTbzJqAyS{LpFIO{U{d2M4FO0LhqyCpxaKxQMDfPY9eiZW3{ub02tLheCm30DlQ2FNUT70sF}}G2WqD172JHyA"
Data "I8pSu26{{AoButyzI4mgzBOxz2Ouxl9O4veIYKzDb3Hfz{d9ZkKcIvpHQvMcLqIm50zlvjIUWmWzm6naMqmmcd7z"
Data "rPSVx2fVQ2EUC9QGoaa5yjwtK5jr6NSOSb}MRRWCwszDw6NUvnoLjahQp9qDk5MqNTWnvRlGlM{GbrtOrkNU0j1M"
Data "TJq8mo2GqNPDbYtx}xzpqRbIYNUOHH{CFibBqvQl8t3yYx8Ee54EOjyd5g{1Z{lMqJUBRWxslfOKlNeNbm7V0mpi"
Data "iozN3rPNIMA9n9x5sguBlHq2FAG2d7WMgXNSVeY5MfUvakULAuLveb8hfYAM49e4C79uF}Fy8gkpkRC{OwlHScmt"
Data "jp8nrrVW4J2QpET6lUJwZliX06rbjCCW24FHJWjs6hF}}qcJZGpgXcwtZJ4nCNiR3mtm0EMUI}il80jTr7laaoNJ"
Data "Bb3pLkhkFDAQRpFM4d8eTQGFkKtqfAzVTHeA3rjXg8j5A8Fw5{OaODNwHMRm28Mh0JlQ}MnKqmkwn6Zp3l9BYmss"
Data "Zvk28LWBzyYR}VGX9Il2lqwKhJkBJlJgoVC4eYQnF7JduOa5l5VSOuWb7ZX}1Q2rt5OOX6406YIhrbNK}}z34OhO"
Data "TQN4iIJO1y49FYmFXPOsb{R2{TV8{i0wJpFd8Ic1bEy4xli032CxYS28IpRzbIFFDdlgAesu5XKgMsKXy7ZYmCLG"
Data "lkyXmavDBtfm9fFLZOCGU05f42DrzLRjE98dzmi3YqHvCc2CPYWbk3KD{OJTsNN6BR7k3eTV0KIxVWWB3ykFzy0g"
Data "7Tc{FmniN7}0pll61R4PtBuaMC1WWdyClAm}}d1mWD8ma8MppY32fC{W2FZ20aaWm6hgDokO{HNSM6TWW9Z{cmXd"
Data "0YisFeYg}t0nS19auCGMpC0QdnkKGL1Rqm01QA{s5G11A7O82TEq}lyRe{o2X4eqC950x2aV3p87PnlpluNeKOBA"
Data "{pt412Y3tkcZ}L5WHEjC3STOB7296PJ3bb7JKvHoDwZlY2gTQAizS8a}H5A}Vb23OoAZsuCKb5}7st27hZa8xgSq"
Data "GpCGL3R8Yg80Pyhtz1Q0zYh90H24WKZ0U70XnF{}q8DH4K9mQQlWXHWlzW4TsfdCbFy8uMWmp1u3w6kyMH5aEHjc"
Data "}Z84DdaJAjspzKrx2rDTVMzyfnPOHUma0APCdXZhUpIwMT1nvE5Z0f6Ed9Ch6d8q2MDKfFq6YCbUWQakxtL1AYsy"
Data "vt4Bc6kv{OoLBS8O07RFyqy1QA{M7G1160PaHlx}OnKOCRORFWwQU15k1DYZZBL}0S8URLbH6kjcncWAu5aOSIaT"
Data "4bSPieGM}V4}VDA5g{13CleXMo{0IAnuPYWw4n9lvOBPmWT09jkMwO}h{bQK}oTqT5WtkyjhU6DEGSpdSUHrm1}A"
Data "F0qv8Q9w{WdTU6FX5c65rzGfwJ2i5sAx0BHNGGh3C0ph8SxSbImmUjp}tzSkf6Hr1fDIQ2zZE}MKYr4KsJ7IMIuB"
Data "{cd16nujIe}m7daee9DNaULvZv5PUTGCphKwv0k31FrNe6xrqDmvrA5}7ZC8JzdZaLUpFD2ne17KG6affHhABt}v"
Data "6SiSnbgvJnGyvwIH2moV9ZA0K3i1lz8kVrFgNuXJ5xOzy6g{hU84YCVGXBJSTft5Yj9TJPj7e11p2WVoHm1Qh6K7"
Data "GHW85bAW3ut}}uyYmfRTKbxkIeWQYJVla{cqqsnd{Yz3B8JvVGSsGYHTiRnJXIzzdb2IUMvxN4aTVHPf4vi2UQvR"
Data "g3EPwmxlosZqn2}eq0JlkcqX4uZxiLE6W8wj}TJtX4dXp5fERxlGHO9Rf7EEcwUkmrNOBS2GVhAcg5PcyXxITPvC"
Data "MKAwIVdgn{8oynNSSMPiGzIMvIk1Vvt}Gm1Qh6Y5OH06rfAArTggzFz}SfjA5RfBiTjW6tkQZn8kCPZaSlzgMRrJ"
Data "IVYdH6CqqkK8uHbwMQHLa77NZiSUilbnzeLfZz8J}b}P2EzoCcz3u08XCvHRjuwfitEdB1nunOuooFIl{FHXnejB"
Data "kY3PvpKA18ULcDQn6lfYHHJMV8KxnjiLI9LM1SOoCw6FTMIHPm9HY0yjPr4bK{XhCt}3kGiW6avG}K9DyB}{OS}n"
Data "lrZ0YG8w5We2w1DK}}wmePZmWmdDP9W9R9126JBuYc8Mge4CoCe90QZfi4uh4HDKa4udmxzVB5ZJ1Qw0W9WD442q"
Data "6tv7Bu1m3Lx36c2uNG8BVvW9wFv1Cm6NHMW0ZuNOJCTVWQ2Q3A6OSEZ0xpw001gTe1fa0220HYDaa06001gDa446"
Data "009e50qo0110esYG0O006aM33004q2WQP00WW4RH80C00JIA1Xa02A1HDCW0G02Dea46009e50qo0110esYG0O00"
Data "eiNmEoc0}0qMY75mCio0QwP4BtLtXq2D1GYWqR6iGGO00cWqGWOP00WW6avG4Gt0Y{NGPW7}VV}1Yp6JWsSF2KZB"
Data "BuWPVxwo8EPQuPj5yEyQ{VONjcS3QHFBHyYgNbG3qS2lVomiC8Bj7uP20djLKEq10u006WmH0jcCmIg8706lVpDW"
Data "1iBFpmmSuCKgM0pvy{aNPEC7U6CAV5}6zn}iB0Z64z0g64m0tde2sYJovWTqimWOV8P5o6SERj3I46oRJZv}1mg}"
Data "}mqatlqHOG0fH05mw}61pyWH0X10WwF8Ok7WOFZz0MavsQ}VB294JiL2mH}OEWBGnXIaVHa8At1xzVqVJYw6BtyO"
Data "Fl}W46JuWh7wYea3Tw225jo3rzp}{yF0XayXiG0WW0P33W5wIuJXrxjVxn0xgBv3LhwH03XaVe3NR}xupIh4y15p"
Data "mOu0XWW2i34ACVpxyPtt5mCVszTVtz5J0XlMdLqGmFuPj72BuI}3BmOM9dmAYGK00PWtWnu6GHWAFnb3cSF}}ozD"
Data "NUeW8bcBCT74tUN0fIaf1TwxVDpZG6IuCCPiKJvhzmBBjQFUvczwUhS9yQx6j69EVt{iAQRM0gSuHnKIoGTB{fKT"
Data "A7xKm21qJkP3IwIQmLOFaOeMkl03UI5bKP9LgRjgDeJ0oNk3CcsKCE9SpDlLOEPzArAL8jYP7iDfmPy0p9l12iCW"
Data "44I78Yafb839}tzW3}4WSRVMI1qReQKXv{nzH1cSlRmvZI7e7P9PV4AGKHIgr}meZv79r21kQUn9}HyOWbPCQHD2"
Data "qOxKK}kNyjkea16kFPClwd5M17lZ3ldSQCSGa{BAOivAtfMdUpmZDD}PXLJ6PFsvqNQ4tSkw7BB34DqBiamN5X5}"
Data "sQot0CoRGGk080o5TwYHWKaY}l}V5q4H59HBsaWe8phdsR71U7kNx}t3G{Jn924EP7a1IrD3xN01vP7M}1jRY}cw"
Data "3E6P5zbQdNA0Sb5SyrOqTttXqhxBkqAiIeJrET5BAKk4GC5EbxAYKKAJ4pgKlYmKBwvMWL2QWinMC0RjSPia8k9p"
Data "4liGW7ahMW4GX84ZTKXcYa7}}uyH{53HpImeekTy4SSs7HFFXxl}D8IAKjgsmFVCYfHjuZWmFouG0PlDDxneh}C4"
Data "JC2GfNU1gymCSVRrgx2S3TncURyMX1PBQdrbM1ydUjkDrin6CbnImLWeZXivcqmfgkS0L1IojYYyu0bRu4xOlvZJ"
Data "BTykW0PFaGK4B3z1cvF}mdV6i7pyWMFR8Jx0nyNo}Zl3Br110MhaCeCotq}ySSZF{}NZ6p5WN7q2jRV0xN1m4vg5"
Data "yAexPc2mCdE0zeF2bNB0fHYOVuDhUt3kOV}ywzSVrj{}VZ6m0oFv3T2Nly5Me4O1G00DdWW1EXAo3PIP6UgeFsz}"
Data "Sp2JkXM74Ren1TUwGUo0YVVxhvzi33{{i0WoGFOs41imh0oWxPsil}547JHZSJ3y{ix0yHjOV7A0FONTXG01D7WY"
Data "19J6}e4Qm73mvRSWzqTOX12xxGrOX8034xK9YdTx0hYBp}Rh9{{}ZmnyeD3ZHbJy13SlSe1DP}cDCmQPu6Pm0CjG"
Data "}i{WAWIe5kQyVsZb84003Pewv3EmGyV40Xf6UVlY8qJe1nit0smhsmTYjtM2KkmtvD3v1gnES6yf1OCtx}w1cwsV"
Data "VtEYRhJzmO2KylzZw8DQ22bscD746O4s}}h10DdUzGsa3EWq3c0M0SKVQFlHvoTlWK9V6L0xiG01c3UZ501188YQ"
Data "k6KVTVz3bcXIAaha9bbYzDq5P15dTbps}KCXOOgLiv6qtCvDvZ3ehXtQB5YrtgsTaiLz38WuGrxayJ54m0EkVUmW"
Data "kZhsKYIPaG8fOP6eISbiNkKcLl8tas4{NY}K8kHcIrbXq0AGN8bD1CtjpDMzJaU3LwPyQeS0p5pR2e0W3Ds3YRUe"
Data "fF}}oaYp4Hsx2jLxZvCDP6xpJ3pxHuPm964oxlHoJjUZ5fgiZJCCz}FDCJ8YVK}EW7Uz2iNia8NypDKfQZGvBpPU"
Data "ngXw1GGq5cGHrFewaZvmigruc5viz}GGpq}jrb4o3Xje70ToTOwLV0GaeDbaCZBMMbmidIphyIg01jq4rPz}89z1"
Data "Tely{mMDXXyeMpb{W8yHmZrF1DlH0LUFIDYm4M7q2Qe}EAe0sDOK1WR43XQY9wZWXcBRAGBp6zonK6C10cXm55b8"
Data "0E8WY0VWxH3Kee9G{mazhIAfnzNsmbaZ8vb5x8OIJ4EnsGeJGtSiG00DZY9h5C01G02DHiWW0m01DEYAhA447XDM"
Data "GZ802A1HDCW0G02Dea46009e50qo0110esYG0O006aM33004q2WQP00WW4RH80C00JIA1Xa02A1HDCW0G02Dea46"
Data "009e50qo0110esYG0O006kW9Afe0N1D7ZubHX4SRFRG01D3eyFiW0m01D8o7t}}z8ZJ91vuBXX7VCVVM}Fhs1zkg"
Data "LV1A6F2aCc8h3xwzH90BMo0JpmG1G00DumZv{f61ImYlYa{}1BaPtZ08SVrxmo21kRVcnnlisY32e4jP45jvm7k9"
Data "S80tu4KNRnZGDXfXtm844433E49{Ck2w}m86M7xz0ILty79FB07nWu4Bntq8R4n}VxzduCQxbOTC0110mpX2G47z"
Data "2{}QN6PWNFm5hGx1lkxm3mCNZbJFny7mxjjxLZPgxD110m01D7mZv1i32nDelIweAlZBmekj}6R}}0TorZiqGi1R"
Data "Uw0RQrYuGLk3}kBSxIdmwyztDiHM7f25FnXu2ApD}lg7{}jF5hvVtu0g8Vg{WY{uGQf}009e{4R8BmPMvb{WLO6W"
Data "UySOVcDBosmCV}xz0Irz}WVUIm3QslDT15mlW0ZGrg1}E2E4YVNFRmR4LXu6Xkz0zR}5yRlf{u{Vly3B0Xb{IK3q"
Data "}Vq6kU063W02QFX6o4y63kOle7MAtnZuMT{0jijX02QMFx}{iBA2DfA0LkR{4lpt4UcU0f5Fl}ZnBDA6cSV7z8sB"
Data "Y{qdmnykzlUuYaZ4hef}Gj{}}{1XU}Vd1qGVoxVFFr6k5m48e64R8S3q2ser}U1W4Fy5MjkEBK8OMFvy{yF90hem"
Data "3bHcS7CpVF}r1nuu75}WQuBn0V1M7Y3DAGJC03ZMwF{6HmWl5zDt3W5GyGRHnyJrxHVAzI800JHi8{Gdw6uEq5K4"
Data "m8pmxXUEnS2WJSqipA2BVq0yLj1Axjy4BrQ0WhyOPk46eDOQfB6ClaQUWUyRwlqx004qR2Fa1tX}A6XlnhTt35oC"
Data "s1l0eBqBh1n0HHZXH1WGkBGD1J3ZNltw1org0G9es0wT}6RW0jqD2SmRNrZ5X704E4YO3PGz4EaIibEGyFQWNp{8"
Data "FMGUF}Z2W7Hr0X001D1mpI4}}Z20Uka}Vl}yzuk7xG{EZN6WXCi2YXPmEVMLuzyF1fP}e9sP12yP{}E1WV0b9OJ0"
Data "Vr1RcONr{oUa0GM001gk4V8J4boilt{oC}pwZFCsBxPVaEs4u63u}kOFi1h}n}Bd9Q6jffy{}cQ0NrB2nF0Toxai"
Data "X04Bmu{Fx9yPUDc}8rt0mm0GGCEroFe9AL20zGCBpw{FZBWICpCeAbYX0oT2Vq1RUvWu67wpuJsP07GK2mER42mI"
Data "k5d}uDj1X1E0zA5VSCF001108s4{1B87Y868{H38rXwOUjZDC0GdDr2ka46r2Uzy2FXO9pGj004qN8iiP42y7pbG"
Data "r1DTQPDRksFJ1n10mxN86h808816qI03004qYWOP00YWKJJ80400ZQ911W02Q1GDCW0GGADea06001f5Wmm01D0e"
Data "6cG08816qI03004qYWOP00YWKJJ804003SlHte44M0{N9mOyWL000JGwk3Z80400ZOJXTxe3b7aElE3hNouqbG01"
Data "D9f3Q00{lSKvPum6lcFCDG84q6Y43380442Z4JB80400ZTOXD0NVVp1mS3By{SRCpV3Z5ni7aC7wxHydQCd5rv{i"
Data "tE3SyFat4nzIjW08eD48eILuyvE1uHiVxv}FxDmClpxpSlpvpFh}3yDtSPRldu3KRz1oh8}0IkI}848BG02D5baq"
Data "1Bz{ieZoCg0iqllD9C3Ay9tXonzEOAo0Ljh}uUD6ESyS8816SmWD0PlaFnQcpoWdLxCAC}tz}HyO1JmiVE05{Noy"
Data "35{HMre00JGQ8RG4JBy{ePsapGGkadw1bW115k{neca12A3H8ekMuCFRtxpOnF6ivGO8eD48eI7uyvLJ5EiLI{1o"
Data "wH}M2ny00cWqGcW8lZ3mCNq5i}w1BdD10F0v0bzUOrjZ3n10etK8JG1ugo8dAyCN9b0yi33yVeMwe9xhsyTlu2ii"
Data "C01008r624q0EFZPUBv{PU06NPB0zUqt6{jN97b{O8vXvSGM{001D3eVGcM0EXy2kum2ypeC{5KSC02T3m48eD4S"
Data "GbC0Mz{E6SouBiu02A3HIdsG0O006esGGGO00cWK3J80442ZQ901W00QHOCC00JGA1fa0220Hj4W0m01D8e66G08"
Data "e54qo01008sYGGO00cWK3J804600n59mLZJ}2rO00000IKLEHAv2O880"
Data "!EOF"
