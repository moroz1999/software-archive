package com.archive.backend.geo.web.mapper;

import com.archive.backend.geo.read.CityItem;
import com.archive.backend.geo.read.CountryItem;
import com.archive.backend.geo.web.dto.CityDto;
import com.archive.backend.geo.web.dto.CountryDto;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface GeoWebMapper {

    List<CountryDto> toCountries(List<CountryItem> countries);

    List<CityDto> toCities(List<CityItem> cities);
}
