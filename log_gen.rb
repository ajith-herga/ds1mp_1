[1..4].each do |i|
	r_file = File.new("log.in"+i.to_s, "r")
	w_file = File.new("log.out", "a")
	log_levels = ["INFO","WARN","DEBUG","ERROR"]
	while (line = r_file.gets)
		r1 = Random.rand(log_levels.size)	
		w_file.puts(Time.at(rand * Time.now.to_i).to_s + " " + log_levels[r1] + "___" + line)
	end
	r_file.close
	w_file.close
end
