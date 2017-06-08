; ID: 3301
; Author: Krischan
; Date: 2016-12-07 14:41:13
; Title: Ingame Screen Resolution Switch
; Description: Ingame Screen Resolution Switch using MaxGUI and miniB3D

SuperStrict

Framework sidesign.MiniB3D
Import brl.pngloader
Import brl.FreeTypeFont
Import maxgui.Drivers
Import maxgui.ProxyGadgets



' ------------------------------------------------------------------------------------------------
' Globals
' ------------------------------------------------------------------------------------------------
Global SCREEN_WIDTH:Int = 640                                   ' Graphics: Screen Width
Global SCREEN_HEIGHT:Int = 480                                  ' Graphics: Screen Height
Global SCREEN_DEPTH:Int = 32                                    ' Graphics: Screen Depth
Global SCREEN_MODE:Int = 2                                      ' Graphics: Screen Mode
Global SCREEN_RATE:Int = 60                                     ' Graphics: Screen Refresh Rate
Global SCREEN_DESKTOPWIDTH:Int = DesktopWidth()                 ' Graphics: Desktop Center horizontal
Global SCREEN_DESKTOPHEIGHT:Int = DesktopHeight()               ' Graphics: Desktop Center vertical
Global SCREEN_CENTERX:Int = SCREEN_DESKTOPWIDTH / 2             ' Graphics: Desktop Center horizontal
Global SCREEN_CENTERY:Int = SCREEN_DESKTOPHEIGHT / 2            ' Graphics: Desktop Center vertical
Global SCREEN_ASPECT:Float = SCREEN_WIDTH * 1.0 / SCREEN_HEIGHT ' Graphivs: Screen Aspect Ratio
Global Scaler:Float                                             ' Graphics: Global Screen Scale Factor

Global MAXGUI_WINDOW:TGadget
Global MAXGUI_CANVAS:TGadget
Global CAMERA:TCam



' ------------------------------------------------------------------------------------------------
' Create Main Window
' ------------------------------------------------------------------------------------------------
MAXGUI_WINDOW = CreateWindow("Resolution Switch Demo", SCREEN_CENTERX - (SCREEN_WIDTH / 2), SCREEN_CENTERY - (SCREEN_HEIGHT / 2), SCREEN_WIDTH, SCREEN_HEIGHT, Null, WINDOW_CLIENTCOORDS)
SetGadgetColor(MAXGUI_WINDOW, 0, 0, 0)



' ------------------------------------------------------------------------------------------------
' Create Graphics Canvas in Main Window
' ------------------------------------------------------------------------------------------------
MAXGUI_CANVAS = CreateCanvas(0, 0, ClientWidth(MAXGUI_WINDOW), ClientHeight(MAXGUI_WINDOW), MAXGUI_WINDOW)
SetGadgetLayout MAXGUI_CANVAS, 1, 1, 1, 1
ActivateGadget(MAXGUI_CANVAS)



' ------------------------------------------------------------------------------------------------
' miniB3D Graphics Mode settings
' ------------------------------------------------------------------------------------------------
TGlobal.width = ClientWidth(MAXGUI_WINDOW)
TGlobal.Height = ClientHeight(MAXGUI_WINDOW)
TGlobal.depth = SCREEN_DEPTH
TGlobal.mode = SCREEN_MODE
TGlobal.rate = SCREEN_RATE



' ------------------------------------------------------------------------------------------------
' Init Canvas Graphics
' ------------------------------------------------------------------------------------------------
SetGraphics CanvasGraphics(MAXGUI_CANVAS)
TGlobal.GraphicsInit()
UpdateScaler()



' ------------------------------------------------------------------------------------------------
' Init Main Camera
' ------------------------------------------------------------------------------------------------
CAMERA:TCam = New TCam
CAMERA.Add()
CAMERA.RangeMin = 0.1
CAMERA.RangeMax = 100000
CAMERA.X = 0
CAMERA.Y = 0
CAMERA.Z = 0
CAMERA.UpdatePosition()
CAMERA.Update()



' ------------------------------------------------------------------------------------------------
' Init Scene
' ------------------------------------------------------------------------------------------------

' Set initial Resolution
SetResolution(800, 600)
EnablePolledInput()

' Main Font
Local font:TImageFont = LoadImageFont("c:\Windows\fonts\Arial.ttf", 256 * Scaler)
SetImageFont font

' light
Local light:TLight = CreateLight(2)
PositionEntity light, 0, 0, 1
LightRange light, 2

' some textured scene primitives
'Local tex:TTexture = LoadTexture("textures/notex.png", 16 + 32)

Local cube1:TMesh = CreateCube()
EntityFX cube1, 2
PositionEntity cube1, -2.5, 0, 5
'EntityTexture cube1, tex

Local sphere:TMesh = CreateSphere(32)
EntityFX sphere, 2
PositionEntity sphere, 0, 0, 5
'EntityTexture sphere, tex

Local cube2:TMesh = CreateCube()
EntityFX cube2, 2
PositionEntity cube2, 2.5, 0, 5
'EntityTexture cube2, tex



' ------------------------------------------------------------------------------------------------
' Main Loop
' ------------------------------------------------------------------------------------------------
While Not AppTerminate()
    
    ' needed for correct font display
    SetGraphics CanvasGraphics(MAXGUI_CANVAS)

    ' user input
    If KeyHit(KEY_ESCAPE) Then End
    If KeyHit(KEY_1) Then SetResolution(800, 600)
    If KeyHit(KEY_2) Then SetResolution(1024, 768)
    If KeyHit(KEY_3) Then SetResolution(1280, 800)
    If KeyHit(KEY_4) Then SetResolution(1366, 768)
    If KeyHit(KEY_5) Then SetResolution(1920, 1080)
    If KeyHit(KEY_6) Then SetResolution(DesktopWidth(), DesktopHeight())
    
    TurnEntity cube1, 1, 1, 1
    TurnEntity sphere, 1, 1, 1
    TurnEntity cube2, 1, 1, 1
        
    RenderWorld
    
    BeginMax2D()
    
    ' needed for correct font display
    SetBlend SOLIDBLEND
        
    ' set current scale
    SetScale(Scaler, Scaler)

    ' text output
    Local txt:String = "Current Resolution: " + SCREEN_WIDTH + "x" + SCREEN_HEIGHT
    Local tw:Int = TextWidth(txt)
    Local th:Int = TextHeight(txt)
    SetColor 0, 255, 0
    SetBlend ALPHABLEND
    DrawText txt, 0, 0
    DrawLine(0, th * Scaler, tw, th * Scaler)
        
    ' reset scale
    SetScale(1.0, 1.0)
    
    EndMax2D()
        
    Flip

Wend

End



' --------------------------------------------------------------------------------
' Fixed BeginMax2D()
' --------------------------------------------------------------------------------
Function BeginMax2D()

    Local x:Int, y:Int, w:Int, h:Int
    GetViewport(x, y, w, h)
        
    glDisable(GL_LIGHTING)
    glDisable(GL_DEPTH_TEST)
    glDisable(GL_SCISSOR_TEST)
    glDisable(GL_FOG)
    glDisable(GL_CULL_FACE)

    glMatrixMode GL_TEXTURE
    glLoadIdentity
        
    glMatrixMode GL_PROJECTION
    glLoadIdentity
    glOrtho 0, GraphicsWidth(), GraphicsHeight(), 0, -1, 1
        
    glMatrixMode GL_MODELVIEW
    glLoadIdentity
        
    SetViewport x, y, w, h
                
    Local MaxTex:Int
    glGetIntegerv(GL_MAX_TEXTURE_UNITS, VarPtr(MaxTex))
        
    For Local Layer:Int = 0 Until MaxTex
        glActiveTexture(GL_TEXTURE0 + Layer)
                    
        glDisable(GL_TEXTURE_CUBE_MAP)
        glDisable(GL_TEXTURE_GEN_S)
        glDisable(GL_TEXTURE_GEN_T)
        glDisable(GL_TEXTURE_GEN_R)
    
        glDisable(GL_TEXTURE_2D)
    
    Next
        
    glActiveTexture(GL_TEXTURE0)
                
    glViewport(0, 0, TGlobal.width, TGlobal.Height)
    glScissor(0, 0, TGlobal.width, TGlobal.Height)
            
End Function



' --------------------------------------------------------------------------------
' Fixed EndMax2D()
' --------------------------------------------------------------------------------
Function EndMax2D()

    glDisable(GL_TEXTURE_CUBE_MAP)
    glDisable(GL_TEXTURE_GEN_S)
    glDisable(GL_TEXTURE_GEN_T)
    glDisable(GL_TEXTURE_GEN_R)
    
    glDisable(GL_TEXTURE_2D)
    
    TGlobal.EnableStates()

End Function



' ------------------------------------------------------------------------------------------------
' Change resolution (ingame)
' ------------------------------------------------------------------------------------------------
Function SetResolution(width:Int, Height:Int, depth:Int = 32, mode:Int = 1)
    
    CAMERA.Update
    
    ' change window gadget size
    SetGadgetShape MAXGUI_WINDOW, SCREEN_CENTERX - (width / 2), SCREEN_CENTERY - (Height / 2), width, Height
    
    ' store new size to miniB3D
    TGlobal.width = width
    TGlobal.Height = Height
    TGlobal.depth = depth
    TGlobal.mode = mode
        
    ' store new size to globals
    SCREEN_WIDTH = width
    SCREEN_HEIGHT = Height
    SCREEN_DEPTH = depth
    SCREEN_MODE = mode
            
    ' reset camera
    CAMERA.width = SCREEN_WIDTH
    CAMERA.Height = SCREEN_HEIGHT
    CAMERA.x = CAMERA.ox
    CAMERA.y = CAMERA.oy
    CAMERA.z = CAMERA.oz
    CAMERA.pitch = CAMERA.Opitch
    CAMERA.yaw = CAMERA.Oyaw
    CAMERA.roll = CAMERA.Oroll
    CAMERA.Update()
                
    ' recalculate Scale
    UpdateScaler()
        
    Print("Set Resolution to " + width + "x" + Height)
        
End Function



' --------------------------------------------------------------------------------
' Update Screen Scale
' --------------------------------------------------------------------------------
Function UpdateScaler()

    Scaler = SCREEN_WIDTH * 1.0 / 2560
    SCREEN_ASPECT = SCREEN_WIDTH * 1.0 / SCREEN_HEIGHT

End Function



' --------------------------------------------------------------------------------
' Custom Camera handling
' --------------------------------------------------------------------------------
Type TCam

    Field cam:TCamera                                       ' camera entity
    Field parent:TEntity                                    ' camera parent

    Field X:Float = 0.0                                     ' Position X
    Field Y:Float = 0.0                                     ' Position Y
    Field Z:Float = 0.0                                     ' Position Z

    Field OX:Float = 0.0                                    ' Old Position X
    Field OY:Float = 0.0                                    ' Old Position Y
    Field OZ:Float = 0.0                                    ' Old Position Z

    Field OPitch:Float = 0.0                                ' Old Pitch
    Field OYaw:Float = 0.0                                  ' Old Yaw
    Field ORoll:Float = 0.0                                 ' Old Roll
    
    Field Pitch:Float = 0.0                                 ' Pitch
    Field Yaw:Float = 0.0                                   ' Yaw
    Field Roll:Float = 0.0                                  ' Roll
    
    Field Zoom:Float = 1.0                                  ' Zoom factor

    Field RangeMin:Float = 1.0                              ' Frustum start
    Field RangeMax:Float = 10000.0                          ' Frustum end
    
    Field ViewX:Int = 0                                     ' Viewport X Position
    Field ViewY:Int = 0                                     ' Viewport Y Position
    Field width:Int = SCREEN_WIDTH                          ' Viewport width
    Field Height:Int = SCREEN_HEIGHT                        ' Viewport height
    
    Field Glob:Int = False                                  ' Local/Global flag


    ' --------------------------------------------------------------------------------------------
    ' Add new camera
    ' --------------------------------------------------------------------------------------------
    Method Add()
    
        cam = CreateCamera(parent)
        
    End Method

    ' --------------------------------------------------------------------------------------------
    ' Update Camera
    ' --------------------------------------------------------------------------------------------
    Method Update()

        OX = EntityX(cam)
        OY = EntityY(cam)
        OZ = EntityZ(cam)

        OPitch = EntityPitch(cam)
        OYaw = EntityYaw(cam)
        ORoll = EntityRoll(cam)
    
        CameraRange cam, rangemin, rangemax
        CameraZoom cam, zoom
        CameraViewport cam, viewx, viewy, width, Height
    
    End Method
    
    ' --------------------------------------------------------------------------------------------
    ' Update Camera Position
    ' --------------------------------------------------------------------------------------------
    Method UpdatePosition()

        PositionEntity cam, x, y, z
    
    End Method
        
    ' --------------------------------------------------------------------------------------------
    ' Store current camera position
    ' --------------------------------------------------------------------------------------------
    Method GetCameraPosition(glob:Int = False)
    
        X = EntityX(cam, glob)
        Y = EntityY(cam, glob)
        Z = EntityZ(cam, glob)
                    
    End Method

End Type
