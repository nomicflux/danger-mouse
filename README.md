# danger-mouse

Error handling for processing pipelines of information in Clojure.

## Usage

```clojure
(catch-errors-> [1 2 3]
  (map inc)
  (map #(if (even? %) % (throw (Exception. (str %)))))
  (map #(* % 10)))

> {:result [20 40]
   :errors [{:error-msg "3"
             :input 2
             :error ....}]}
```

## License

Copyright © 2021 Michael Anderson
