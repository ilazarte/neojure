(ns neojure.dom)

; Just a few simple operations wrapping d3 in case no other dom api is wanted
; most operations work on a selection, generally one item

(defn select 
  "d3 select first wrapper" 
  ([selector]
    (js/d3.select selector))
  ([selection selector]
    (.select selection selector)))

(defn select-all
  "d3 select all wrapper"
  [selector]
  (js/d3.selectAll selector))

(defn append 
  [selection tagname]
  (.append selection tagname))

(defn attr 
  [selection key val]
  (.attr selection key val))

(defn style 
  [selection val]
  (attr selection "style" val))

(defn size [selection]
  (.size selection))

(defn put-by-tag
  "Add an element if it does not exist
   The element will be added to the parent selection"
  [selection tagname]
  (let [el  (select selection tagname)
        len (size el)]
    (if (= len 1)
      el
      (-> selection (append tagname)))))

(defn put-by-id 
  "Add an element if it does not exist
   The element is given the id and added to the parent selection"
  [selection tagname id]
  (let [el  (select selection (str "#" id))
        len (size el)]
    (if (= len 1)
      el
      (-> selection (append tagname) (attr "id" id)))))
