const int send_encrypted_data(
	int fd,
	const char* server_name, 
	const void *src,
	const size_t src_size,
	void *dst,
	const size_t dst_size
);