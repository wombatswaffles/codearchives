; ID: 3304
; Author: col
; Date: 2016-12-10 02:43:55
; Title: LRU and MRU Cache
; Description: Least recently used cache

Type TCache
	Field _map:TMap
	Field _list:TList
	
	Method New()
		_map = New TMap
		_list = New TList
	EndMethod
	
	Method hit(obj:Object)
		Local link:TLink = TLink(_map.valueforkey(obj))
		
		' add to the cache if not in it
		If Not link
			Local link:TLink = _list.addfirst(obj)
			_map.insert(obj,link)
			Return
		EndIf

		' remove link from list
		link._succ._pred = link._pred
		link._pred._succ = link._succ
		
		' move link to the front
		link._pred = _list._head
		link._succ = _list._head._succ
		link._succ._pred = link
		_list._head._succ = link
	EndMethod
	
	Method dropLRU:Object()
		Return _list.removelast()
	EndMethod
	
	Method dropMRU:Object()
		Return _list.removefirst()
	EndMethod
	
	Method getLRU:Object()
		Return _list.last()
	EndMethod
	
	Method getMRU:Object()
		Return _list.first()
	EndMethod
	
	Method clear()
		_map.clear()
		_list.clear()
	EndMethod
EndType



' EXAMPLE USE
		
Local cache:TCache = New TCache

cache.hit("a")
cache.hit("b")
cache.hit("c")
cache.hit("d")
cache.hit("e")

cache.hit("d")
cache.hit("a")
cache.hit("c")
cache.hit("d")
cache.hit("c")

Print
Print "Dropping '" + String(cache.dropLRU()) + "' from the cache"
Print

' show cache contents
Print "Cache contents:"
For Local i:String = EachIn cache._list
	WriteStdout i + " "
Next

Print
Print
Print "Most recent used item: " + String(cache.getMRU())
