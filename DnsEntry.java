  // Used to store a hostname bound to an IP address for DNS caching
  class DnsEntry {
    // The host name of the stored entry
    String host;
    // The address of the stored host
    String address;
    // The local time in ms when the entry was created
    long timestamp;

    public DnsEntry(String host, String address, long current_time){

      this.host = host;
      this.address = address;
      this.timestamp = current_time;

    }

    public String getAddress(){
      return this.address;
    }

    // Returns whether or not this entry is older than its TTL (hardcoded to 30 seconds as instructed)
    public boolean isLive(long current_time){

      // If the address was cached more than 30 seconds ago then it is no longer live.
      if (current_time - this.timestamp < 30000){
        return true;
      } 
      return false;
    }

  }