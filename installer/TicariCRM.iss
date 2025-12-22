; Inno Setup Configuration for TicariCRM
; Professional Windows Desktop Application Installer
; This creates a commercial-grade Setup.exe that handles everything

#define MyAppName "Ticari CRM"
#define MyAppVersion "1.0.0"
#define MyAppPublisher "TicariCRM"
#define MyAppURL "https://ticaricrm.com"
#define MyAppExeName "start-application.bat"
#define MyAppMainJar "Crm-1.0-SNAPSHOT.jar"

[Setup]
; Basic Application Information
AppId={{A8F94E22-1D55-4B89-B0C7-7E2E3F4D5A6B}
AppName={#MyAppName}
AppVersion={#MyAppVersion}
AppVerName={#MyAppName} {#MyAppVersion}
AppPublisher={#MyAppPublisher}
AppPublisherURL={#MyAppURL}
AppSupportURL={#MyAppURL}/support
AppUpdatesURL={#MyAppURL}/updates
DefaultDirName={autopf}\{#MyAppName}
DefaultGroupName={#MyAppName}
OutputBaseFilename=TicariCRM_Setup_{#MyAppVersion}
OutputDir=..\dist

; Installer UI Configuration
WizardStyle=modern
; SetupIconFile=..\src\main\resources\icon.ico  ; Icon file is optional - commented out if missing

; Compression
Compression=lzma2/ultra64
SolidCompression=yes

; Privileges and Architecture
PrivilegesRequired=admin
ArchitecturesAllowed=x64
ArchitecturesInstallIn64BitMode=x64

; Disk Space Requirements
ExtraDiskSpaceRequired=8589934592

; Uninstall Configuration
UninstallDisplayIcon={app}\{#MyAppExeName}
CreateUninstallRegKey=yes

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[Components]
Name: "core"; Description: "Core Application Files"; Types: full compact custom; Flags: fixed
Name: "jre"; Description: "Java Runtime Environment 21"; Types: full compact; Flags: fixed
Name: "sqlserver"; Description: "Microsoft SQL Server Express 2022"; Types: full

[Files]
Source: "..\target\{#MyAppMainJar}"; DestDir: "{app}"; Flags: ignoreversion; Components: core
Source: "..\target\lib\*"; DestDir: "{app}\lib"; Flags: ignoreversion recursesubdirs; Components: core
Source: "..\jre\*"; DestDir: "{app}\jre"; Flags: ignoreversion recursesubdirs; Components: jre
Source: "..\bootstrap\*.ps1"; DestDir: "{app}\bootstrap"; Flags: ignoreversion; Components: core
Source: "..\start-application.bat"; DestDir: "{app}"; Flags: ignoreversion; Components: core
Source: "..\README.md"; DestDir: "{app}\docs"; Flags: ignoreversion; Components: core

[Icons]
Name: "{group}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"
Name: "{autodesktop}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"

[Run]
Filename: "powershell.exe"; Parameters: "-ExecutionPolicy Bypass -File ""{app}\bootstrap\bootstrap-launcher.ps1"" -SkipUI"; Flags: runhidden waituntilterminated

[Code]
function IsSQLServerInstalled: Boolean;
var
  ResultCode: Integer;
begin
  Result := Exec('sc.exe', 'query MSSQL$SQLEXPRESS', '', SW_HIDE, ewWaitUntilTerminated, ResultCode) and (ResultCode = 0);
end;
