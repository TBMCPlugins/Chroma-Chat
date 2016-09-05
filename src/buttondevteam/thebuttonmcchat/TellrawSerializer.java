package buttondevteam.thebuttonmcchat;

import java.io.IOException;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class TellrawSerializer extends TypeAdapter<TellrawSerializableEnum> {

	@Override
	public TellrawSerializableEnum read(JsonReader reader) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void write(JsonWriter writer, TellrawSerializableEnum enumval) throws IOException {
		writer.value(enumval.getName());
	}

}
