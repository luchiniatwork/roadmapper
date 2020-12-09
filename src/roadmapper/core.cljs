(ns roadmapper.core
  (:require [cljs-http.client :as http]
            [cljs.core.async :refer [<! go]]
            [clojure.string :as s]))

(enable-console-print!)

(defmulti to-ms last)

(defmethod to-ms :days [[n & _]]
  (* n 24 60 60 1000))

(defmethod to-ms :day [[n & _]]
  (* n 24 60 60 1000))

(defmethod to-ms :weeks [[n & _]]
  (* 7 (to-ms [n :days])))

(defmethod to-ms :week [[n & _]]
  (* 7 (to-ms [n :days])))

(defmethod to-ms :months [[n & _]]
  (* 4 (to-ms [n :weeks])))

(defmethod to-ms :month [[n & _]]
  (* 4 (to-ms [n :weeks])))

(defmethod to-ms :quarters [[n & _]]
  (* 3 (to-ms [n :months])))

(defmethod to-ms :quarter [[n & _]]
  (* 3 (to-ms [n :months])))

(defmethod to-ms :default [_]
  nil)

(defn parse-effort [effort]
  (let [parts (s/split effort #" ")]
    [(-> parts first js/parseInt) (-> parts last keyword)]))

(defmulti parse-date
  (fn [date-str]
    (let [parts (s/split date-str #"/")]
      (cond (= 3 (count parts))
            :full-date

            (and (= 2 (count parts)) (= "Q" (ffirst parts)))
            :quarter

            (and (= 2 (count parts)))
            :month))))

(defmethod parse-date :quarter [date-str]
  (let [[quarter year] (s/split date-str #"/")
        month (condp = quarter
                "Q1" 1 "Q2" 4
                "Q3" 7 "Q4" 10)
        year' (js/parseInt year)
        month' (dec month)]
    (js/Date. year' month' 1)))

(defmethod parse-date :month [date-str]
  (let [[month year] (s/split date-str #"/")
        months {"jan" 1 "feb" 2 "mar" 3 "apr" 4 "may" 5 "jun" 6
                "jul" 7 "aug" 8 "sep" 9 "oct" 10 "nov" 11 "dec" 12}
        month' (->> month s/lower-case (get months) dec)
        year' (js/parseInt year)]
    (js/Date. year' month' 1)))

(defmethod parse-date :full-date [date-str]
  (let [[year month day] (s/split date-str #"/")
        day' (-> day js/parseInt)
        month' (-> month js/parseInt dec)
        year' (-> year js/parseInt)]
    (js/Date. year' month' day')))

(defmethod parse-date :default [date-str]
  nil)

(defn set-columns [dt]
  (doto dt
    (.addColumn "string" "Task ID")
    (.addColumn "string" "Task Name")
    (.addColumn "string" "Resource")
    (.addColumn "date" "Start Date")
    (.addColumn "date" "End Date")
    (.addColumn "number" "Duration")
    (.addColumn "number" "Percent Complete")
    (.addColumn "string" "Dependencies")))

(defn fetch-data []
  (go
    (let [{:keys [status body]} (<! (http/get "roadmap.edn"))]
      (if (= 200 status)
        body
        (throw (ex-info "Can't find roadmap.edn file" {}))))))

(defn description [{:keys [description resource]}]
  (if resource
    (str description " (" resource ")")
    resource))

(defn dependencies [{:keys [follows]}]
  (s/join "," follows))

(defn duration [{:keys [effort]}]
  (-> effort parse-effort to-ms))

(defn format-data [{:keys [tasks]}]
  (map (fn [{:keys [id group start end completed]
             :as task}]
         [id (description task) group
          (parse-date start) (parse-date end) (duration task)
          0 (dependencies task)])
       tasks))

(defn ^:export draw-chart []
  (go
    (let [chart (google.visualization.Gantt. (js/document.getElementById "chart_div"))
          data (<! (fetch-data))]
      (println (format-data data))
      (.draw chart
             (doto (google.visualization.DataTable.)
               set-columns
               (.addRows
                (clj->js (format-data data))))
             (clj->js {:height (* 55 (count (:tasks data)))})))))

(google.charts.load "current" (clj->js {:packages ["gantt"]}))
(google.charts.setOnLoadCallback draw-chart)
