; ID: 3299
; Author: Krischan
; Date: 2016-11-26 18:01:07
; Title: Localization using SQLite and TMap
; Description: Localization using SQLite and TMap

SuperStrict

Import brl.Map
Import bah.DBSQLite

Global LOCALE:TLocale = New TLocale

' create test database
LOCALE.Create

' english
LOCALE.language = "EN"
LOCALE.Load
Print "English:"
Print LOCALE.Get("GAME_TITLE")
Print LOCALE.Get("SOLAR_MAINSEQUENCE")
Print LOCALE.Get("SOLAR_GIANT")
Print LOCALE.Get("SOLAR_WHITEDWARF")
Print LOCALE.Get("SOLAR_YEARS")

Print

' german
LOCALE.language = "DE"
LOCALE.Load
Print "German:"
Print LOCALE.Get("GAME_TITLE")
Print LOCALE.Get("SOLAR_MAINSEQUENCE")
Print LOCALE.Get("SOLAR_GIANT")
Print LOCALE.Get("SOLAR_WHITEDWARF")
Print LOCALE.Get("SOLAR_YEARS")

Type TLocale

	Global Locale:TMap = CreateMap()			' Global TMap with all text
	Global db:TDBConnection						' database connection
	Global q:TDatabaseQuery						' database query
	
	Field language:String = "EN"				' the current locale Flag
	Field database:String = "locale"			' the database file name
	
	Method Create()

		DeleteFile database + ".db"
		
		db = LoadDatabase("SQLITE", database + ".db")
		
		db.executeQuery("CREATE TABLE locale (id integer primary key AUTOINCREMENT, category varchar(32), key varchar(32), EN text, DE text)")
		db.executeQuery("INSERT INTO locale Values (NULL,'GAME','TITLE','Extrasolar Origin','Extrasolar Origin')")
		db.executeQuery("INSERT INTO locale Values (NULL,'SOLAR','MAINSEQUENCE','Main Sequence','Hauptreihe')")
		db.executeQuery("INSERT INTO locale Values (NULL,'SOLAR','GIANT','Giant','Riese')")
		db.executeQuery("INSERT INTO locale Values (NULL,'SOLAR','WHITEDWARF','White Dwarf','Weiﬂer Zwerg')")
		db.executeQuery("INSERT INTO locale Values (NULL,'SOLAR','YEARS','Years','Jahre')")
								
		db.close()
	
	End Method

	Method Load()
		
		db = LoadDatabase("SQLITE", database + ".db")
		If Not db Then db.close()
		
		q = db.executeQuery("SELECT * from locale")
	
		While q.nextRow()

			' get record
			Local r:TQueryRecord = q.rowRecord()
			Local cat:String = r.getStringByName("category")
			Local Key:String = r.getStringByName("key")
			
			MapInsert(LOCALE, cat + "_" + Key, r.getStringByName(language))
									
		Wend
		
		db.close()
				
	End Method

	' ------------------------------------------------------------------------------------------------
	' Returns Locale Object
	' ------------------------------------------------------------------------------------------------
	Method Get:String(Key:String)
	
		Return String(MapValueForKey(LOCALE, Key))
			
	End Method

End Type
