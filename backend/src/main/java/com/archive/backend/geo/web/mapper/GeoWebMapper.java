package com.archive.backend.geo.web.mapper;

import com.archive.backend.geo.read.CityItem;
import com.archive.backend.geo.read.CountryItem;
import com.archive.backend.geo.web.dto.CityDto;
import com.archive.backend.geo.web.dto.CountryDto;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface GeoWebMapper {

    List<CountryDto> toCountries(List<CountryItem> countries);

    List<CityDto> toCities(List<CityItem> cities);
}
